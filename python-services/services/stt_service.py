import whisper
import os
from typing import Optional, Dict, Any
from langdetect import detect

class STTService:
    """
    Speech-to-Text service using OpenAI Whisper.
    Supports multilingual transcription with EN/ES/CA optimization.
    """

    def __init__(self):
        self.models = {}

    def get_whisper_model(self, language: str):
        """
        Load appropriate Whisper model based on language.
        EN/ES use medium model, CA uses large model for better accuracy.
        """
        if language in ["en", "es"]:
            model_size = "medium"
        elif language == "ca":
            model_size = "large"
        else:
            model_size = "large"

        if model_size not in self.models:
            print(f"Loading Whisper {model_size} model...")
            self.models[model_size] = whisper.load_model(model_size)

        return self.models[model_size]

    def get_transcription_prompt(self, language: str, subject: Optional[str]) -> str:
        """Generate optimized transcription prompt for better accuracy."""
        prompts = {
            "en": {
                "cs": "This is a university computer science lecture about data structures, algorithms, and programming.",
                "math": "This is a university mathematics lecture covering algebra, calculus, and analysis.",
                "physics": "This is a university physics lecture on mechanics, thermodynamics, and quantum physics.",
                "default": "This is a university lecture."
            },
            "es": {
                "cs": "Esta es una clase universitaria de informática sobre estructuras de datos, algoritmos y programación.",
                "math": "Esta es una clase universitaria de matemáticas sobre álgebra, cálculo y análisis.",
                "physics": "Esta es una clase universitaria de física sobre mecánica, termodinámica y física cuántica.",
                "default": "Esta es una clase universitaria."
            },
            "ca": {
                "cs": "Aquesta és una classe universitària d'informàtica sobre estructures de dades, algorismes i programació.",
                "math": "Aquesta és una classe universitària de matemàtiques sobre àlgebra, càlcul i anàlisi.",
                "physics": "Aquesta és una classe universitària de física sobre mecànica, termodinàmica i física quàntica.",
                "default": "Aquesta és una classe universitària."
            }
        }

        lang_prompts = prompts.get(language, prompts["en"])
        return lang_prompts.get(subject, lang_prompts["default"])

    def transcribe(self, audio_path: str, language: str = "auto",
                   course_subject: Optional[str] = None) -> Dict[str, Any]:
        """
        Transcribe audio file to text with language detection.

        Args:
            audio_path: Path to audio file
            language: Language code (en/es/ca/auto)
            course_subject: Course subject for optimized prompts (cs/math/physics)

        Returns:
            Dict with transcription results including detected language and segments
        """
        try:
            model = self.get_whisper_model(language if language != "auto" else "en")
            initial_prompt = self.get_transcription_prompt(
                language if language != "auto" else "en",
                course_subject
            )

            if language == "auto":
                result = model.transcribe(audio_path, initial_prompt=initial_prompt)
                detected_lang = result.get('language', 'en')
            else:
                result = model.transcribe(
                    audio_path,
                    language=language,
                    initial_prompt=initial_prompt
                )
                detected_lang = language

            for segment in result['segments']:
                try:
                    segment['detected_lang'] = detect(segment['text'])
                except:
                    segment['detected_lang'] = detected_lang

            return {
                "success": True,
                "language": detected_lang,
                "text": result['text'],
                "segments": result['segments'],
                "duration": result.get('duration', 0)
            }

        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }
        finally:
            if os.path.exists(audio_path):
                os.remove(audio_path)
