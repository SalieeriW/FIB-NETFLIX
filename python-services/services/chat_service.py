from langchain_community.llms import Ollama
from langdetect import detect
import chromadb
from typing import Dict, Any, Optional
from sentence_transformers import SentenceTransformer

class ChatService:
    """
    Multilingual chatbot service with cross-lingual retrieval.
    Automatically detects question language and responds accordingly.
    """

    def __init__(self, chroma_path: str = "/tmp/vidstream/rag_knowledge",
                 model_name: str = "qwen2.5:7b"):
        self.llm = Ollama(model=model_name)
        self.chroma_client = chromadb.PersistentClient(path=chroma_path)
        # Use the same embedding model as EmbeddingService
        self.embedding_model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')
        print(f"Chat service initialized with model: {model_name}")

    def chat(self, course_id: int, question: str,
             language: Optional[str] = None) -> Dict[str, Any]:
        """
        Answer questions about course content with source citations.

        Args:
            course_id: Course identifier
            question: User question in any language (EN/ES/CA)
            language: Optional forced response language (auto-detect if None)

        Returns:
            Dict with answer, sources, and language metadata
        """
        try:
            if language is None:
                language = detect(question)

            collection_name = f"course_{course_id}_mixed"
            
            # Check if collection exists
            try:
                collection = self.chroma_client.get_collection(collection_name)
            except Exception as e:
                return {
                    "success": False,
                    "error": f"Course content not found. Please process the course first. Collection '{collection_name}' does not exist."
                }
            
            # Generate embedding for question using the same model as stored embeddings
            question_embedding = self.embedding_model.encode([question])[0].tolist()
            
            # Use query_embeddings instead of query_texts to avoid dimension mismatch
            results = collection.query(query_embeddings=[question_embedding], n_results=5)

            # Check if we have any results
            if not results['documents'] or len(results['documents'][0]) == 0:
                return {
                    "success": False,
                    "error": "No relevant content found in the course material to answer this question."
                }

            context = "\n\n".join(results['documents'][0])
            sources = results['metadatas'][0] if results['metadatas'] and len(results['metadatas'][0]) > 0 else []

            retrieved_langs = [meta.get('lang', 'unknown') for meta in sources] if sources else []

            prompt = self._get_chat_prompt(language, context, question)
            answer = self.llm.invoke(prompt)

            return {
                "success": True,
                "question": question,
                "question_lang": language,
                "answer": answer,
                "sources": sources,
                "retrieved_langs": retrieved_langs
            }

        except Exception as e:
            import traceback
            error_trace = traceback.format_exc()
            print(f"Error in chat service for course {course_id}: {error_trace}")
            return {
                "success": False,
                "error": str(e)
            }

    def _get_chat_prompt(self, language: str, context: str, question: str) -> str:
        """Generate prompt for chat response."""
        prompts = {
            "en": f"""
Based only on the following course content, answer the question.

Course content (multilingual):
{context}

Question: {question}

Instructions:
- Answer in clear, concise English
- Use information from all languages in the context
- If answer not in content, say "I don't find that information in the course material"
- Cite sources when possible (timestamp, slide/page number)
- Use an educational and friendly tone

Answer:
""",
            "es": f"""
Basándote únicamente en el siguiente contenido del curso, responde la pregunta.

Contenido del curso (multilingüe):
{context}

Pregunta: {question}

Instrucciones:
- Responde en español claro y conciso
- Usa información de todos los idiomas en el contexto
- Si la respuesta no está en el contenido, di "No encuentro esa información en el material del curso"
- Cita fuentes cuando sea posible (timestamp, número de diapositiva/página)
- Usa un tono educativo y amigable

Respuesta:
""",
            "ca": f"""
Basant-te únicament en el següent contingut del curs, respon la pregunta.

Contingut del curs (multilingüe):
{context}

Pregunta: {question}

Instruccions:
- Respon en català clar i concís
- Utilitza informació de tots els idiomes del context
- Si la resposta no està al contingut, digues "No trobo aquesta informació al material del curs"
- Cita fonts quan sigui possible (timestamp, número de diapositiva/pàgina)
- Utilitza un to educatiu i amigable

Resposta:
"""
        }

        return prompts.get(language, prompts["en"])
