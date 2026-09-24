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
from langchain_community.document_loaders import PyPDFLoader, TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_core.prompts import ChatPromptTemplate
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_classic.chains.retrieval import create_retrieval_chain
from langchain_core.documents import Document

load_dotenv()

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
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

# Load Gemini LLM and Embeddings
llm = ChatGoogleGenerativeAI(model="gemini-3.6-flash", google_api_key=os.getenv("GEMINI_API_KEY"))
embeddings = GoogleGenerativeAIEmbeddings(model="models/gemini-embedding-2", google_api_key=os.getenv("GEMINI_API_KEY"))

# In-memory store for Vector DBs per project
vector_stores = {}
vector_db_dir = "./chroma_db"

class ChatRequest(BaseModel):
    projectId: int
    question: str

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
                # Speech to text processing
                text = extract_audio_and_transcribe(temp_file_path)
                docs.append(Document(page_content=text, metadata={"source": obj.object_name, "type": "speech-to-text"}))
                
    return docs

def build_vector_store(project_id: int):
    print(f"Analyzing data for project {project_id}...")
    docs = load_documents_from_minio(project_id)
    
    if not docs:
        return None
        
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
    splits = text_splitter.split_documents(docs)
    
    collection_name = f"project_{project_id}"
    
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
                if attempt < max_retries - 1:
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
        retriever = vectorstore.as_retriever(search_type="similarity", search_kwargs={"k": 5})

        # 2. Setup Prompt
        system_prompt = (
            "Bạn là một trợ lý ảo phân tích tài liệu của dự án. "
            "Sử dụng các thông tin sau để trả lời câu hỏi của người dùng. "
            "Nếu thông tin không có trong tài liệu, hãy nói là không tìm thấy.\n\n"
            "{context}"
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            ("human", "{input}"),
        ])

        # 3. Create RAG Chain
        question_answer_chain = create_stuff_documents_chain(llm, prompt)
        rag_chain = create_retrieval_chain(retriever, question_answer_chain)

        # 4. Generate Response
        response = rag_chain.invoke({"input": req.question})
        return {"answer": response["answer"]}
    
    except Exception as e:
        print("ERROR:", e)
        raise HTTPException(status_code=500, detail=str(e))
