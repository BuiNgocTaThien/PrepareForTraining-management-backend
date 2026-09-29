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
from langchain_openai import ChatOpenAI
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_community.document_loaders import PyPDFLoader, TextLoader, Docx2txtLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
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

# Load LLM for chat from 9Router local proxy (credentials from .env)
llm = ChatOpenAI(
    model=os.getenv("OPENAI_MODEL", "cx/gpt-6-luna"),
    api_key=os.getenv("OPENAI_API_KEY"),
    base_url=os.getenv("OPENAI_BASE_URL", "http://localhost:20128/v1"),
    max_tokens=8192,
    timeout=int(os.getenv("OPENAI_TIMEOUT_SECONDS", "600"))
)

# Use HuggingFace Local Embeddings for zero-cost, infinite document processing
print("Loading Local Embedding Model (HuggingFace)...")
embeddings = HuggingFaceEmbeddings(model_name="paraphrase-multilingual-MiniLM-L12-v2")

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
    documentSource: Optional[List[str]] = []

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

def load_documents_from_minio(project_id: int, specific_objects=None):
    if specific_objects is None:
        prefix = f"project_{project_id}/"
        objects = minio_client.list_objects(BUCKET_NAME, prefix=prefix, recursive=True)
        object_names = [obj.object_name for obj in objects if not obj.object_name.endswith('/')]
    else:
        object_names = specific_objects
        
    docs = []
    # Temporary directory to save files for loaders and whisper
    with tempfile.TemporaryDirectory() as temp_dir:
        for obj_name in object_names:
            file_extension = obj_name.split('.')[-1].lower()
            try:
                response = minio_client.get_object(BUCKET_NAME, obj_name)
                content = response.read()
                response.close()
                response.release_conn()
            except Exception as e:
                print(f"Error reading {obj_name}: {e}")
                continue

            temp_file_path = os.path.join(temp_dir, os.path.basename(obj_name))
            with open(temp_file_path, "wb") as f:
                f.write(content)

            try:
                if file_extension == 'pdf':
                    loader = PyPDFLoader(temp_file_path)
                    loaded_docs = loader.load()
                    for doc in loaded_docs:
                        doc.metadata["source"] = obj_name
                    print(f"Loaded {len(loaded_docs)} pages from PDF: {obj_name}")
                    docs.extend(loaded_docs)
                elif file_extension in ['txt', 'md']:
                    loader = TextLoader(temp_file_path, encoding="utf-8")
                    loaded_docs = loader.load()
                    for doc in loaded_docs:
                        doc.metadata["source"] = obj_name
                    docs.extend(loaded_docs)
                elif file_extension == 'docx':
                    loader = Docx2txtLoader(temp_file_path)
                    loaded_docs = loader.load()
                    for doc in loaded_docs:
                        doc.metadata["source"] = obj_name
                    print(f"Loaded DOCX: {obj_name}")
                    docs.extend(loaded_docs)
                elif file_extension in ['mp4', 'mp3', 'wav']:
                    # Tạm thời bỏ qua Video/Audio
                    print(f"Skipping media file {obj_name} to prevent server freeze.")
                    docs.append(Document(page_content=f"Tài liệu {obj_name} là file video/audio, hiện không hỗ trợ phân tích trực tiếp.", metadata={"source": obj_name}))
            except Exception as e:
                print(f"Error parsing {obj_name}: {e}")
                
    return docs

def build_vector_store(project_id: int):
    print(f"Syncing data for project {project_id} (Delta Update)...")
    collection_name = f"project_{project_id}"
    vectorstore = Chroma(
        collection_name=collection_name,
        embedding_function=embeddings,
        persist_directory=vector_db_dir
    )
    
    # Get existing sources from Chroma
    existing_data = vectorstore.get()
    existing_ids = existing_data['ids']
    existing_metadatas = existing_data['metadatas']
    
    existing_sources = set()
    for meta in existing_metadatas:
        if meta and 'source' in meta:
            existing_sources.add(meta['source'])
            
    # Get current files in MinIO
    prefix = f"project_{project_id}/"
    minio_objects = minio_client.list_objects(BUCKET_NAME, prefix=prefix, recursive=True)
    minio_sources = set([obj.object_name for obj in minio_objects if not obj.object_name.endswith('/')])
    
    # 1. Delete removed files from Chroma
    sources_to_delete = existing_sources - minio_sources
    if sources_to_delete:
        print(f"Removing deleted files from Chroma: {sources_to_delete}")
        ids_to_delete = []
        for doc_id, meta in zip(existing_ids, existing_metadatas):
            if meta and meta.get('source') in sources_to_delete:
                ids_to_delete.append(doc_id)
        if ids_to_delete:
            vectorstore.delete(ids=ids_to_delete)
            
    # 2. Add new files to Chroma
    sources_to_add = minio_sources - existing_sources
    if sources_to_add:
        print(f"Adding new files to Chroma: {sources_to_add}")
        new_docs = load_documents_from_minio(project_id, specific_objects=list(sources_to_add))
        if new_docs:
            text_splitter = RecursiveCharacterTextSplitter(chunk_size=1500, chunk_overlap=200)
            splits = text_splitter.split_documents(new_docs)
            vectorstore.add_documents(splits)
            print(f"Successfully added {len(splits)} chunks from new documents.")
    else:
        print("No new documents to add.")
        
    return vectorstore

def get_or_build_vector_store(project_id: int):
    if project_id in vector_stores:
        return vector_stores[project_id]
        
    collection_name = f"project_{project_id}"
    
    # Try loading from disk first
    vs = Chroma(collection_name=collection_name, persist_directory=vector_db_dir, embedding_function=embeddings)
    if vs._collection.count() > 0:
        print(f"Loaded existing vector store from disk for project {project_id} ({vs._collection.count()} chunks)")
        vector_stores[project_id] = vs
        return vs
        
    # If not on disk, build it
    vs = build_vector_store(project_id)
    if vs:
        vector_stores[project_id] = vs
    return vs

@app.post("/api/v1/chat")
async def chat(req: ChatRequest):
    try:
        # 1. Retrieve or load the Vector DB from disk (RAG step 1)
        vectorstore = get_or_build_vector_store(req.projectId)
        if not vectorstore:
            return {"answer": "Dự án này chưa có tài liệu nào."}
            
        # Tăng tối đa số đoạn lấy ra để đảm bảo AI đọc HẾT toàn bộ tài liệu (lên tới 100 đoạn)
        search_kwargs = {"k": 100, "fetch_k": 200}
        if req.documentSource and len(req.documentSource) > 0:
            if len(req.documentSource) == 1:
                search_kwargs["filter"] = {"source": req.documentSource[0]}
            else:
                search_kwargs["filter"] = {"source": {"$in": req.documentSource}}
            
        retriever = vectorstore.as_retriever(search_type="mmr", search_kwargs=search_kwargs)

        # 2. Setup Prompt
        system_prompt = (
            "Bạn là 9Router AI - một Trợ lý Trí tuệ Nhân tạo chuyên nghiệp, uyên bác và tận tâm, được thiết kế để phân tích tài liệu và hỗ trợ người dùng giải quyết các vấn đề phức tạp.\n\n"
            "MỤC TIÊU CỐT LÕI:\n"
            "Cung cấp câu trả lời xuất sắc, chính xác tuyệt đối dựa trên tài liệu được cung cấp, đồng thời giữ văn phong lịch sự, khách quan và dễ hiểu.\n\n"
            "NGUYÊN TẮC HOẠT ĐỘNG (BẮT BUỘC TUÂN THỦ):\n"
            "1. XỬ LÝ TÀI LIỆU CHUẨN XÁC: Phần 'TÀI LIỆU DỰ ÁN (Context)' bên dưới CHÍNH LÀ nội dung file mà người dùng đang đính kèm hoặc yêu cầu phân tích. Tuyệt đối không trả lời 'không thấy file'.\n"
            "2. CHỐNG ẢO GIÁC (ZERO HALLUCINATION): Mọi thông tin bạn đưa ra phải được trích xuất 100% từ tài liệu. Nếu tài liệu không có thông tin để trả lời, hãy trung thực phản hồi: 'Tài liệu hiện tại không đề cập đến vấn đề này' thay vì tự bịa đặt.\n"
            "3. HIỆU SUẤT & TRỰC DIỆN: Bỏ qua các câu chào hỏi sáo rỗng hoặc lặp lại câu hỏi. Đi thẳng vào trọng tâm vấn đề ngay ở câu đầu tiên.\n"
            "4. TRÌNH BÀY CHUYÊN NGHIỆP: Luôn định dạng câu trả lời bằng Markdown một cách có tính thẩm mỹ cao:\n"
            "   - Sử dụng tiêu đề (H2, H3) để chia bố cục nếu câu trả lời dài.\n"
            "   - Sử dụng gạch đầu dòng (-) hoặc đánh số (1, 2, 3) để liệt kê.\n"
            "   - **In đậm** các thuật ngữ quan trọng hoặc kết luận chính.\n"
            "   - Sử dụng blockquote (>) cho các trích dẫn và code block (```) nếu có mã nguồn.\n"
            "5. BỐI CẢNH ĐỘC LẬP: Nếu lịch sử chat có nhắc đến các chủ đề cũ không liên quan đến tài liệu hiện tại, hãy chủ động bỏ qua chúng để không làm nhiễu câu trả lời.\n\n"
            "TÀI LIỆU DỰ ÁN (Context):\n"
            "{context}"
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            MessagesPlaceholder(variable_name="chat_history"),
            ("human", "{input}"),
        ])

        # 3. Create RAG Chain
        document_prompt = PromptTemplate(
            input_variables=["page_content", "source"],
            template="[Trích xuất từ file: {source}]\n{page_content}"
        )
        question_answer_chain = create_stuff_documents_chain(llm, prompt, document_prompt=document_prompt)
        rag_chain = create_retrieval_chain(retriever, question_answer_chain)

        # Build chat_history list (Sliding Window: only keep the last 10 messages)
        chat_history_messages = []
        if req.history:
            recent_history = req.history[-10:] # Prevent memory overflow (429 Quota Exceeded)
            for msg in recent_history:
                if msg.role == "user":
                    chat_history_messages.append(HumanMessage(content=msg.content))
                else:
                    chat_history_messages.append(AIMessage(content=msg.content))

        # 4. Generate Response
        docs_retrieved = retriever.invoke(req.question)
        print(f"DEBUG: documentSource filter={search_kwargs.get('filter')}")
        print(f"DEBUG: Retrieved {len(docs_retrieved)} documents from Chroma.")
        
        question_to_ask = req.question
        if req.documentSource and len(req.documentSource) > 0:
            question_to_ask += "\n\n(Lưu ý: Bạn đang đọc đúng file mà tôi đã chỉ định. Hãy đóng vai trò chuyên gia để phân tích ngay, không giải thích dài dòng hay báo lỗi thiếu file)."
        
        response = rag_chain.invoke({
            "input": question_to_ask,
            "chat_history": chat_history_messages
        })
        
        answer = response["answer"]
        
        return {"answer": answer}
    
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

@app.get("/api/v1/chroma-sources/{project_id}")
async def get_chroma_sources(project_id: int):
    vs = get_or_build_vector_store(project_id)
    if not vs:
        return {"sources": []}
    res = vs.get()
    sources = set()
    for m in res.get("metadatas", []):
        if m and "source" in m:
            sources.add(m["source"])
    return {"sources": list(sources)}
