from langchain_community.llms import Ollama
from typing import Dict, Any, Optional
import chromadb
from sentence_transformers import SentenceTransformer

class RAGService:
    """
    RAG (Retrieval Augmented Generation) service for course note generation.
    Supports multilingual content generation in EN/ES/CA.
    """

    def __init__(self, chroma_path: str = "/tmp/vidstream/rag_knowledge",
                 model_name: str = "qwen2.5:7b"):
        self.llm = Ollama(model=model_name)
        self.chroma_client = chromadb.PersistentClient(path=chroma_path)
        # Use the same embedding model as EmbeddingService
        self.embedding_model = SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')
        print(f"RAG service initialized with model: {model_name}")

    def generate_notes(self, course_id: int, language: str = "en",
                      include_sources: bool = True) -> Dict[str, Any]:
        """
        Generate structured course notes in specified language.

        Args:
            course_id: Course identifier
            language: Target language (en/es/ca)
            include_sources: Whether to include source citations

        Returns:
            Dict with generated notes and metadata
        """
        try:
            collection_name = f"course_{course_id}_mixed"
            print(f"Generating notes for course {course_id} in {language}")
            
            collection = self.chroma_client.get_collection(collection_name)
            print(f"Collection '{collection_name}' found")

            query = self._get_summary_query(language)
            print(f"Querying collection with: {query}")
            
            # Generate embedding for query using the same model as stored embeddings
            query_embedding = self.embedding_model.encode([query])[0].tolist()
            print(f"Generated query embedding with dimension: {len(query_embedding)}")
            
            # Use query_embeddings instead of query_texts to avoid dimension mismatch
            results = collection.query(query_embeddings=[query_embedding], n_results=30)
            
            if not results['documents'] or len(results['documents'][0]) == 0:
                return {
                    "success": False,
                    "error": f"No documents found in collection for course {course_id}"
                }
            
            context = "\n\n".join(results['documents'][0])
            print(f"Retrieved {len(results['documents'][0])} documents for context")
            print(f"Context length: {len(context)} characters")

            prompt = self._get_notes_prompt(language, context, include_sources)
            print("Invoking LLM to generate notes...")
            notes = self.llm.invoke(prompt)
            print(f"Notes generated successfully, length: {len(notes) if notes else 0}")

            return {
                "success": True,
                "language": language,
                "notes": notes,
                "sources_count": len(results['documents'][0]) if include_sources else 0
            }

        except Exception as e:
            import traceback
            error_trace = traceback.format_exc()
            print(f"Error in generate_notes for course {course_id}: {error_trace}")
            return {
                "success": False,
                "error": str(e),
                "traceback": error_trace
            }

    def _get_summary_query(self, language: str) -> str:
        """Get query for retrieving course summary content."""
        queries = {
            "en": "Summarize all key concepts, formulas, and examples from this course",
            "es": "Resume todos los conceptos clave, fórmulas y ejemplos de este curso",
            "ca": "Resumeix tots els conceptes clau, fórmules i exemples d'aquest curs"
        }
        return queries.get(language, queries["en"])

    def _get_notes_prompt(self, language: str, context: str, include_sources: bool) -> str:
        """Generate prompt for notes generation."""
        source_instruction = {
            "en": "\n- Cite sources with [Source: Video HH:MM:SS, Slide N]" if include_sources else "",
            "es": "\n- Cita fuentes con [Fuente: Video HH:MM:SS, Diapositiva N]" if include_sources else "",
            "ca": "\n- Cita fonts amb [Font: Vídeo HH:MM:SS, Diapositiva N]" if include_sources else ""
        }

        prompts = {
            "en": f"""
Generate structured course notes in English based on the following content:

{context}

Required format:
# Course Title

## 1. Key Concepts
- List main concepts
- Brief explanation of each

## 2. Important Formulas/Algorithms
- Present key formulas or algorithms
- Explain variables and complexity

## 3. Examples
- Include examples from lecture
- Show step-by-step solutions

## 4. Summary
- Key takeaways
- Important points to remember

IMPORTANT:
- Write in formal academic English
- Use correct technical terminology
- Only include information from provided content
- Do not invent data not present{source_instruction["en"]}
""",
            "es": f"""
Genera apuntes estructurados en español basados en el siguiente contenido:

{context}

Formato requerido:
# Título del Curso

## 1. Conceptos Clave
- Lista de conceptos principales
- Explicación breve de cada uno

## 2. Fórmulas/Algoritmos Importantes
- Presenta fórmulas o algoritmos clave
- Explica variables y complejidad

## 3. Ejemplos
- Incluye ejemplos de la clase
- Muestra soluciones paso a paso

## 4. Resumen
- Puntos clave para recordar
- Conceptos importantes

IMPORTANTE:
- Escribe en español académico formal
- Usa terminología técnica correcta
- Solo incluye información del contenido proporcionado
- No inventes datos que no estén presentes{source_instruction["es"]}
""",
            "ca": f"""
Genera apunts estructurats en català basats en el següent contingut:

{context}

Format requerit:
# Títol del Curs

## 1. Conceptes Clau
- Llista de conceptes principals
- Explicació breu de cadascun

## 2. Fórmules/Algorismes Importants
- Presenta fórmules o algorismes clau
- Explica variables i complexitat

## 3. Exemples
- Inclou exemples de la classe
- Mostra solucions pas a pas

## 4. Resum
- Punts clau per recordar
- Conceptes importants

IMPORTANT:
- Escriu en català acadèmic formal
- Utilitza terminologia tècnica correcta
- Només inclou informació del contingut proporcionat
- No inventis dades que no siguin presents{source_instruction["ca"]}
"""
        }

        return prompts.get(language, prompts["en"])
