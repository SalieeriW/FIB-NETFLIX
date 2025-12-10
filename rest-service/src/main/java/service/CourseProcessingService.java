package service;

import database.CourseDAO;
import database.CourseNotesDAO;
import database.TranscriptDAO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service to process courses: video->audio->transcription->embeddings->notes
 */
public class CourseProcessingService {

    private static final String PYTHON_SERVICE_URL =
        System.getenv("PYTHON_SERVICE_URL") != null ?
        System.getenv("PYTHON_SERVICE_URL") : "http://localhost:5001";

    /**
     * Process course asynchronously: extract audio, transcribe, create embeddings
     */
    public static void processCourseAsync(int courseId, String videoPath,
                                         String pdfPath, String language) {
        CompletableFuture.runAsync(() -> {
            try {
                processCourse(courseId, videoPath, pdfPath, language);
            } catch (Exception e) {
                System.err.println("Error processing course " + courseId + ": " + e.getMessage());
                e.printStackTrace();
                CourseDAO.updateCourseStatus(courseId, "ERROR");
            }
        });
    }

    /**
     * Main course processing pipeline
     */
    public static void processCourse(int courseId, String videoPath,
                                    String pdfPath, String language) throws Exception {

        System.out.println("=== Processing Course " + courseId + " ===");
        CourseDAO.updateCourseStatus(courseId, "PROCESSING");

        // Step 1: Extract audio from video
        String audioPath = extractAudio(videoPath, courseId);
        System.out.println("✓ Audio extracted: " + audioPath);

        // Step 2: Transcribe audio
        String transcriptJson = transcribeAudio(audioPath, language, courseId);
        System.out.println("✓ Audio transcribed");

        // Step 3: Extract PDF text (if provided)
        String pdfText = null;
        if (pdfPath != null && !pdfPath.isEmpty()) {
            pdfText = extractPdfText(pdfPath);
            System.out.println("✓ PDF text extracted");
        }

        // Step 4: Create text chunks
        List<String> chunks = createChunks(transcriptJson, pdfText);
        System.out.println("✓ Created " + chunks.size() + " text chunks");

        // Step 5: Generate embeddings and store in vector DB
        createEmbeddings(courseId, chunks);
        System.out.println("✓ Embeddings created and stored");

        // Step 6: Generate notes in all three languages
        generateAllNotes(courseId);
        System.out.println("✓ Notes generated in EN/ES/CA");

        CourseDAO.updateCourseStatus(courseId, "READY");
        System.out.println("=== Course " + courseId + " processing complete ===");
    }

    /**
     * Extract audio from video using FFmpeg
     */
    private static String extractAudio(String videoPath, int courseId) throws Exception {
        String audioPath = "/tmp/vidstream/audio/course_" + courseId + ".mp3";

        File audioDir = new File("/tmp/vidstream/audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", videoPath,
            "-vn",
            "-acodec", "libmp3lame",
            "-ar", "16000",
            "-y",
            audioPath
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("FFmpeg output: " + output);
            throw new Exception("FFmpeg failed with exit code: " + exitCode);
        }

        return audioPath;
    }

    /**
     * Transcribe audio using Python STT service
     */
    private static String transcribeAudio(String audioPath, String language,
                                         int courseId) throws Exception {

        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            throw new FileNotFoundException("Audio file not found: " + audioPath);
        }

        String boundary = "----Boundary" + System.currentTimeMillis();
        URL url = new URL(PYTHON_SERVICE_URL + "/api/stt/transcribe?language=" + language);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream();
             FileInputStream fis = new FileInputStream(audioFile)) {

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"audio_file\"; filename=\"")
                  .append(audioFile.getName()).append("\"\r\n");
            writer.append("Content-Type: audio/mpeg\r\n\r\n");
            writer.flush();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();

            writer.append("\r\n--").append(boundary).append("--\r\n");
            writer.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("STT service returned error: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        String jsonResponse = response.toString();

        // Save transcript to database (simplified - you can parse JSON for details)
        TranscriptDAO.insertTranscript(courseId, "Transcript text", jsonResponse, language, null);

        return jsonResponse;
    }

    /**
     * Extract text from PDF using Apache PDFBox
     */
    private static String extractPdfText(String pdfPath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Create text chunks for embedding
     */
    private static List<String> createChunks(String transcriptJson, String pdfText) {
        List<String> chunks = new ArrayList<>();

        // Extract text from transcript JSON
        if (transcriptJson != null && !transcriptJson.isEmpty()) {
            String transcriptText = extractTextFromTranscript(transcriptJson);
            if (transcriptText != null && !transcriptText.isEmpty()) {
                chunks.addAll(chunkText(transcriptText, "transcript"));
            }
        }

        // Simple chunking - split by sentences/paragraphs (max 500 chars)
        if (pdfText != null && !pdfText.isEmpty()) {
            chunks.addAll(chunkText(pdfText, "pdf"));
        }

        // If no chunks created, add a placeholder to avoid empty list error
        if (chunks.isEmpty()) {
            System.out.println("Warning: No chunks created from transcript or PDF. Adding placeholder.");
            chunks.add("No content available for this course.");
        }

        return chunks;
    }

    /**
     * Extract text from Whisper transcript JSON
     * Expected format: {"success": true, "text": "...", "segments": [...]}
     */
    private static String extractTextFromTranscript(String transcriptJson) {
        try {
            // Look for "text" field in JSON
            // Format: "text":"...actual text..."
            int textKeyIndex = transcriptJson.indexOf("\"text\"");
            if (textKeyIndex == -1) {
                System.out.println("Warning: 'text' field not found in transcript JSON");
                return null;
            }
            
            // Find the colon after "text"
            int colonIndex = transcriptJson.indexOf(":", textKeyIndex);
            if (colonIndex == -1) {
                System.out.println("Warning: Could not find colon after 'text' field");
                return null;
            }
            
            // Skip whitespace after colon
            int valueStart = colonIndex + 1;
            while (valueStart < transcriptJson.length() && 
                   Character.isWhitespace(transcriptJson.charAt(valueStart))) {
                valueStart++;
            }
            
            // Find the opening quote
            if (valueStart >= transcriptJson.length() || 
                transcriptJson.charAt(valueStart) != '"') {
                System.out.println("Warning: Text value does not start with quote");
                return null;
            }
            
            // Find the closing quote (handle escaped quotes)
            int quoteStart = valueStart + 1;
            int quoteEnd = quoteStart;
            while (quoteEnd < transcriptJson.length()) {
                char c = transcriptJson.charAt(quoteEnd);
                if (c == '"' && transcriptJson.charAt(quoteEnd - 1) != '\\') {
                    break;
                }
                quoteEnd++;
            }
            
            if (quoteEnd >= transcriptJson.length()) {
                System.out.println("Warning: Could not find closing quote for text value");
                return null;
            }
            
            String text = transcriptJson.substring(quoteStart, quoteEnd);
            // Unescape JSON
            text = text.replace("\\n", "\n")
                      .replace("\\r", "\r")
                      .replace("\\t", "\t")
                      .replace("\\\"", "\"")
                      .replace("\\\\", "\\");
            
            if (text.isEmpty()) {
                System.out.println("Warning: Extracted text is empty");
                return null;
            }
            
            return text;
        } catch (Exception e) {
            System.err.println("Error extracting text from transcript: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> chunkText(String text, String source) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("\\. ");

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > 500 && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(sentence).append(". ");
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    /**
     * Create embeddings via Python service
     */
    private static void createEmbeddings(int courseId, List<String> chunks) throws Exception {
        StringBuilder jsonChunks = new StringBuilder("[");

        for (int i = 0; i < chunks.size(); i++) {
            jsonChunks.append("{\"text\":\"")
                     .append(escapeJson(chunks.get(i)))
                     .append("\",\"metadata\":{\"lang\":\"en\",\"chunk_id\":")
                     .append(i)
                     .append("}}");
            if (i < chunks.size() - 1) {
                jsonChunks.append(",");
            }
        }
        jsonChunks.append("]");

        String requestBody = String.format(
            "{\"course_id\":%d,\"chunks\":%s}",
            courseId, jsonChunks
        );

        callPythonService("/api/embedding/create", requestBody);
    }

    /**
     * Generate notes in all three languages and save to database
     */
    private static void generateAllNotes(int courseId) throws Exception {
        String notesEn = null;
        String notesEs = null;
        String notesCa = null;
        
        for (String lang : new String[]{"en", "es", "ca"}) {
            String requestBody = String.format(
                "{\"course_id\":%d,\"language\":\"%s\",\"include_sources\":true}",
                courseId, lang
            );
            String pythonResponse = callPythonService("/api/rag/generate_notes", requestBody);
            
            // Extract notes from response
            String notes = extractNotesFromResponse(pythonResponse);
            if (notes != null) {
                switch (lang) {
                    case "en" -> notesEn = notes;
                    case "es" -> notesEs = notes;
                    case "ca" -> notesCa = notes;
                }
            }
        }
        
        // Save all notes to database
        CourseNotesDAO.CourseNotes existingNotes = CourseNotesDAO.getNotesByCourseId(courseId);
        if (existingNotes == null) {
            CourseNotesDAO.insertNotes(courseId, notesEn, notesEs, notesCa);
        } else {
            if (notesEn != null) CourseNotesDAO.updateNotes(courseId, "en", notesEn);
            if (notesEs != null) CourseNotesDAO.updateNotes(courseId, "es", notesEs);
            if (notesCa != null) CourseNotesDAO.updateNotes(courseId, "ca", notesCa);
        }
    }
    
    private static String extractNotesFromResponse(String jsonResponse) {
        try {
            // Simple JSON parsing to extract "notes" field
            int notesStart = jsonResponse.indexOf("\"notes\":\"");
            if (notesStart == -1) {
                return null;
            }
            
            notesStart += 9; // Skip past "notes":"
            int notesEnd = notesStart;
            boolean escaped = false;
            
            while (notesEnd < jsonResponse.length()) {
                char c = jsonResponse.charAt(notesEnd);
                if (escaped) {
                    escaped = false;
                    notesEnd++;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    notesEnd++;
                    continue;
                }
                if (c == '"') {
                    break;
                }
                notesEnd++;
            }
            
            if (notesEnd >= jsonResponse.length()) {
                return null;
            }
            
            String notes = jsonResponse.substring(notesStart, notesEnd);
            // Unescape JSON string
            notes = notes.replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t")
                        .replace("\\\\", "\\");
            
            return notes;
        } catch (Exception e) {
            System.err.println("Error extracting notes from response: " + e.getMessage());
            return null;
        }
    }

    private static String callPythonService(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(PYTHON_SERVICE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Python service returned error: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}
