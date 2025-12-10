from fastapi import FastAPI, UploadFile, HTTPException, File
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Optional
from pydantic import BaseModel
import os

# Disable telemetry for ChromaDB and other libraries
os.environ["ANONYMIZED_TELEMETRY"] = "False"
os.environ["DO_NOT_TRACK"] = "1"
os.environ["CHROMA_TELEMETRY"] = "0"

from services.stt_service import STTService
from services.embedding_service import EmbeddingService
from services.rag_service import RAGService
from services.chat_service import ChatService

app = FastAPI(title="Multilingual RAG Education System")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

stt_service = STTService()
embedding_service = EmbeddingService()
rag_service = RAGService()
chat_service = ChatService()

class EmbeddingRequest(BaseModel):
    course_id: int
    chunks: List[dict]

class NotesRequest(BaseModel):
    course_id: int
    language: str = "en"
    include_sources: bool = True

class ChatRequest(BaseModel):
    course_id: int
    question: str
    language: Optional[str] = None

@app.get("/api/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "rag-education-system",
        "version": "1.0.0",
        "models": {
            "stt": "whisper",
            "embeddings": "paraphrase-multilingual-mpnet-base-v2",
            "llm": "qwen2.5:7b"
        }
    }

@app.post("/api/stt/transcribe")
async def transcribe_audio(
    audio_file: UploadFile = File(...),
    language: str = "auto",
    course_subject: Optional[str] = None
):
    """Transcribe audio file to text with language detection."""
    try:
        audio_path = f"/tmp/{audio_file.filename}"
        with open(audio_path, "wb") as f:
            content = await audio_file.read()
            f.write(content)

        result = stt_service.transcribe(audio_path, language, course_subject)
        return result

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/embedding/create")
async def create_embeddings(request: EmbeddingRequest):
    """Generate embeddings and store in vector database."""
    try:
        result = embedding_service.create_embeddings(request.course_id, request.chunks)
        if not result["success"]:
            error_msg = result.get("error", "Unknown error")
            print(f"Error creating embeddings: {error_msg}")
            raise HTTPException(status_code=500, detail=error_msg)
        return result
    except Exception as e:
        print(f"Exception in create_embeddings: {type(e).__name__}: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/embedding/search")
async def search_embeddings(
    course_id: int,
    query: str,
    n_results: int = 5,
    language_filter: Optional[str] = None
):
    """Perform semantic search in course content."""
    result = embedding_service.search(course_id, query, n_results, language_filter)
    if not result["success"]:
        raise HTTPException(status_code=500, detail=result.get("error"))
    return result

@app.post("/api/rag/generate_notes")
async def generate_notes(request: NotesRequest):
    """Generate structured course notes in specified language."""
    try:
        result = rag_service.generate_notes(
            request.course_id,
            request.language,
            request.include_sources
        )
        if not result["success"]:
            error_msg = result.get("error", "Unknown error")
            print(f"Error generating notes: {error_msg}")
            raise HTTPException(status_code=500, detail=error_msg)
        return result
    except HTTPException:
        raise
    except Exception as e:
        print(f"Exception in generate_notes: {type(e).__name__}: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/chat")
async def chat(request: ChatRequest):
    """Answer questions about course content."""
    result = chat_service.chat(request.course_id, request.question, request.language)
    if not result["success"]:
        raise HTTPException(status_code=500, detail=result.get("error"))
    return result

if __name__ == "__main__":
    import uvicorn
    import os
    port = int(os.getenv("PYTHON_SERVICE_PORT", "5001"))
    uvicorn.run(app, host="0.0.0.0", port=port)
