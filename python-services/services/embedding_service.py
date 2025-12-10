import os
# Disable telemetry before importing libraries
os.environ["ANONYMIZED_TELEMETRY"] = "False"
os.environ["DO_NOT_TRACK"] = "1"
os.environ["CHROMA_TELEMETRY"] = "0"

from sentence_transformers import SentenceTransformer
import chromadb
from typing import List, Dict, Any

class EmbeddingService:
    """
    Multilingual embedding service using sentence-transformers.
    Supports cross-lingual semantic search for EN/ES/CA.
    """

    def __init__(self, chroma_path: str = "/tmp/vidstream/rag_knowledge"):
        self.model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')
        # Disable telemetry in ChromaDB client
        try:
            self.chroma_client = chromadb.PersistentClient(
                path=chroma_path,
                anonymized_telemetry=False
            )
        except TypeError:
            # Fallback for older ChromaDB versions
            self.chroma_client = chromadb.PersistentClient(path=chroma_path)
        print("Multilingual embedding model loaded successfully")

    def create_embeddings(self, course_id: int, chunks: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Generate embeddings for text chunks and store in Chroma vector database.

        Args:
            course_id: Course identifier
            chunks: List of dicts with 'text' and 'metadata' keys

        Returns:
            Dict with success status and collection info
        """
        try:
            # Validate input
            if not chunks or len(chunks) == 0:
                error_msg = "No chunks provided. Cannot create embeddings from empty list."
                print(f"Error in create_embeddings for course {course_id}: {error_msg}")
                return {
                    "success": False,
                    "error": error_msg
                }
            
            # Validate chunk format and filter valid chunks
            valid_chunks = []
            for i, chunk in enumerate(chunks):
                if not isinstance(chunk, dict):
                    print(f"Warning: Chunk {i} is not a dict, skipping: {type(chunk)}")
                    continue
                if 'text' not in chunk or not chunk.get('text') or not str(chunk.get('text')).strip():
                    print(f"Warning: Chunk {i} missing or empty 'text' field, skipping")
                    continue
                valid_chunks.append(chunk)
            
            if len(valid_chunks) == 0:
                error_msg = "No valid chunks found. All chunks must have a non-empty 'text' field."
                print(f"Error in create_embeddings for course {course_id}: {error_msg}")
                return {
                    "success": False,
                    "error": error_msg
                }
            
            collection_name = f"course_{course_id}_mixed"
            collection = self.chroma_client.get_or_create_collection(collection_name)

            texts = [chunk['text'] for chunk in valid_chunks]
            metadatas = [chunk.get('metadata', {}) for chunk in valid_chunks]

            print(f"Creating embeddings for {len(texts)} chunks for course {course_id}")
            embeddings = self.model.encode(texts)

            collection.add(
                documents=texts,
                embeddings=embeddings.tolist(),
                metadatas=metadatas,
                ids=[f"chunk_{i:04d}" for i in range(len(texts))]
            )

            return {
                "success": True,
                "collection": collection_name,
                "chunks_added": len(texts)
            }

        except Exception as e:
            import traceback
            error_trace = traceback.format_exc()
            print(f"Error in create_embeddings for course {course_id}: {error_trace}")
            return {
                "success": False,
                "error": str(e),
                "traceback": error_trace
            }

    def search(self, course_id: int, query: str, n_results: int = 5,
               language_filter: str = None) -> Dict[str, Any]:
        """
        Perform semantic search in the vector database.
        Supports cross-lingual retrieval (e.g., EN query → ES/CA results).

        Args:
            course_id: Course identifier
            query: Search query in any language (EN/ES/CA)
            n_results: Number of results to return
            language_filter: Optional language filter (en/es/ca)

        Returns:
            Dict with search results and metadata
        """
        try:
            collection_name = f"course_{course_id}_mixed"
            collection = self.chroma_client.get_collection(collection_name)

            where_filter = {"lang": language_filter} if language_filter else None

            # Generate embedding for query using the same model as stored embeddings
            query_embedding = self.model.encode([query])[0].tolist()
            
            # Use query_embeddings instead of query_texts to avoid dimension mismatch
            results = collection.query(
                query_embeddings=[query_embedding],
                n_results=n_results,
                where=where_filter
            )

            return {
                "success": True,
                "documents": results['documents'][0],
                "metadatas": results['metadatas'][0],
                "distances": results['distances'][0] if 'distances' in results else []
            }

        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }

    def get_collection_stats(self, course_id: int) -> Dict[str, Any]:
        """Get statistics about a course collection."""
        try:
            collection_name = f"course_{course_id}_mixed"
            collection = self.chroma_client.get_collection(collection_name)
            count = collection.count()

            return {
                "success": True,
                "collection": collection_name,
                "document_count": count
            }
        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }
