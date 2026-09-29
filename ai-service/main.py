import os
import io
import shutil
import tempfile
import time
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from minio import Minio
from dotenv import load_dotenv

# LangChain components
from langchain_google_genai import ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.document_loaders import PyPDFLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_classic.chains.retrieval import create_retrieval_chain
from langchain_core.documents import Document
from langchain_core.messages import HumanMessage, AIMessage
from typing import List, Optional

load_dotenv(override=True)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

minio_client = Minio(
    os.getenv("MINIO_ENDPOINT", "localhost:9000"),
    access_key=os.getenv("MINIO_ACCESS_KEY", "minioadmin"),
    secret_key=os.getenv("MINIO_SECRET_KEY", "minioadmin"),
    secure=False
)
BUCKET_NAME = os.getenv("MINIO_BUCKET_NAME", "preparefortraining-bucket")

# Load Gemini LLM for chat (kept for smart responses)
llm = ChatGoogleGenerativeAI(model="gemini-3.8-flash", google_api_key=os.getenv("GEMINI_API_KEY"), max_output_tokens=8192)

# Use HuggingFace Local Embeddings for zero-cost, infinite document processing
print("Loading Local Embedding Model (HuggingFace)...")
embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")

# In-memory store for Vector DBs per project
vector_stores = {}
vector_db_dir = "./chroma_db"

class Message(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    projectId: int
    question: str
    history: Optional[List[Message]] = []

def extract_audio_and_transcribe(file_path: str):
    """
    Speech-to-text functionality for Video/Audio files.
    Requires `openai-whisper` and `ffmpeg` installed on the system.
    """
    try:
        import whisper
        # Load small model for speed
        model = whisper.load_model("base")
        result = model.transcribe(file_path)
        return result["text"]
    except Exception as e:
        print("Speech-to-text Error (requires openai-whisper and ffmpeg):", e)
        return "Bản ghi âm không thể đọc do thiếu thư viện hệ thống (ffmpeg)."

def load_documents_from_minio(project_id: int):
    prefix = f"project_{project_id}/"
    objects = minio_client.list_objects(BUCKET_NAME, prefix=prefix, recursive=True)
    
    docs = []
    # Temporary directory to save files for loaders and whisper
    with tempfile.TemporaryDirectory() as temp_dir:
        for obj in objects:
            if obj.object_name.endswith('/'): continue
            
            file_extension = obj.object_name.split('.')[-1].lower()
            response = minio_client.get_object(BUCKET_NAME, obj.object_name)
            content = response.read()
            response.close()
            response.release_conn()

            temp_file_path = os.path.join(temp_dir, os.path.basename(obj.object_name))
            with open(temp_file_path, "wb") as f:
                f.write(content)

            if file_extension == 'pdf':
                loader = PyPDFLoader(temp_file_path)
                docs.extend(loader.load())
            elif file_extension in ['txt', 'md']:
                loader = TextLoader(temp_file_path, encoding="utf-8")
                docs.extend(loader.load())
            elif file_extension in ['mp4', 'mp3', 'wav']:
                # Tạm thời bỏ qua Video/Audio vì chạy Whisper trên CPU sẽ làm treo server (mất vài tiếng)
                print(f"Skipping media file {obj.object_name} to prevent server freeze.")
                docs.append(Document(page_content=f"Tài liệu {obj.object_name} là file video/audio, hiện không hỗ trợ phân tích trực tiếp.", metadata={"source": obj.object_name}))
                
    return docs

def build_vector_store(project_id: int):
    print(f"Analyzing data for project {project_id}...")
    docs = load_documents_from_minio(project_id)
    
    if not docs:
        return None
        
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1500, chunk_overlap=200)
    splits = text_splitter.split_documents(docs)
    
    collection_name = f"project_{project_id}"
    
    # Try to delete existing collection to avoid duplicates when reloading
    try:
        temp_vs = Chroma(collection_name=collection_name, persist_directory=vector_db_dir, embedding_function=embeddings)
        temp_vs.delete_collection()
    except Exception:
        pass
    
    # Initialize empty vector store
    vectorstore = Chroma(
        collection_name=collection_name,
        embedding_function=embeddings,
        persist_directory=vector_db_dir
    )
    
    # Process in batches to avoid rate limits (100 RPM)
    batch_size = 50
    for i in range(0, len(splits), batch_size):
        batch = splits[i:i + batch_size]
        print(f"Embedding batch {i//batch_size + 1} of {(len(splits) - 1)//batch_size + 1}...")
        
        # Retry logic for each batch just in case it hits rate limit slightly early
        max_retries = 3
        for attempt in range(max_retries):
            try:
                vectorstore.add_documents(batch)
                break
            except Exception as e:
                print(f"Gemini API Error: {str(e)}")
                if "429" in str(e) and attempt < max_retries - 1:
                    print(f"Rate limit hit. Retrying in 30 seconds... (Attempt {attempt + 1})")
                    time.sleep(30)
                else:
                    raise e
                    
        # Sleep to respect rate limits if there are more batches
        if i + batch_size < len(splits):
            print("Sleeping for 60 seconds to avoid Gemini API Rate Limits...")
            time.sleep(60)

    return vectorstore

@app.post("/api/v1/chat")
async def chat(req: ChatRequest):
    try:
        # 1. Retrieve or build the Vector DB (RAG step 1)
        if req.projectId not in vector_stores:
            vs = build_vector_store(req.projectId)
            if vs:
                vector_stores[req.projectId] = vs
            else:
                return {"answer": "Dự án này chưa có tài liệu nào."}

        vectorstore = vector_stores[req.projectId]
        retriever = vectorstore.as_retriever(search_type="mmr", search_kwargs={"k": 5, "fetch_k": 15})

        # 2. Setup Prompt
        system_prompt = (
            "Bạn là một Chuyên gia Kỹ thuật và Đào tạo (Technical & Training Lead) của dự án. "
            "Nhiệm vụ của bạn là hỗ trợ nhân sự bằng cách trả lời câu hỏi dựa trên các tài liệu được cung cấp dưới đây.\n\n"
            "QUY TẮC TRẢ LỜI:\n"
            "1. CHUYÊN NGHIỆP & RÕ RÀNG: Trình bày mạch lạc, cấu trúc tốt. Luôn sử dụng Markdown (gạch đầu dòng, in đậm từ khóa, chia đoạn dễ đọc).\n"
            "2. CHÍNH XÁC TUYỆT ĐỐI: Chỉ sử dụng thông tin trong phần Tài liệu (Context) bên dưới. Nếu thông tin không có, hãy nói rõ 'Tôi không tìm thấy thông tin này trong tài liệu hiện tại', tuyệt đối KHÔNG tự bịa đặt.\n"
            "3. CHI TIẾT & ĐẦY ĐỦ: Câu trả lời phải trọn vẹn, giải thích cặn kẽ dựa trên tài liệu, không được ngắt quãng giữa chừng.\n\n"
            "TÀI LIỆU DỰ ÁN (Context):\n"
            "{context}"
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            MessagesPlaceholder(variable_name="chat_history"),
            ("human", "{input}"),
        ])

        # 3. Create RAG Chain
        question_answer_chain = create_stuff_documents_chain(llm, prompt)
        rag_chain = create_retrieval_chain(retriever, question_answer_chain)

        # Build chat_history list
        chat_history_messages = []
        if req.history:
            for msg in req.history:
                if msg.role == "user":
                    chat_history_messages.append(HumanMessage(content=msg.content))
                else:
                    chat_history_messages.append(AIMessage(content=msg.content))

        # 4. Generate Response
        response = rag_chain.invoke({
            "input": req.question,
            "chat_history": chat_history_messages
        })
        return {"answer": response["answer"]}
    
    except Exception as e:
        print("ERROR:", e)
        raise HTTPException(status_code=500, detail=str(e))

class ReloadRequest(BaseModel):
    projectId: int

@app.post("/api/v1/reload")
async def reload_knowledge(req: ReloadRequest):
    try:
        print(f"Force reloading knowledge for project {req.projectId}")
        vs = build_vector_store(req.projectId)
        if vs:
            vector_stores[req.projectId] = vs
            return {"message": "Đã nạp lại kiến thức thành công."}
        else:
            if req.projectId in vector_stores:
                del vector_stores[req.projectId]
            return {"message": "Dự án này không có tài liệu nào để nạp."}
    except Exception as e:
        print("ERROR:", e)
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/v1/debug")
def debug_minio(project_id: int):
    prefix = f"project_{project_id}/"
    objects = minio_client.list_objects(BUCKET_NAME, prefix=prefix, recursive=True)
    return {"files": [obj.object_name for obj in objects]}
