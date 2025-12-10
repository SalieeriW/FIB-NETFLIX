package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import util.JwtSessionHelper;
import util.RestClient;
import util.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/uploadVideo")
@MultipartConfig(
    maxFileSize = 524288000,      // 500 MB
    maxRequestSize = 524288000,   // 500 MB
    fileSizeThreshold = 1048576   // 1 MB
)
public class uploadVideo extends HttpServlet {

    private static final String UPLOAD_DIR = "/tmp/vidstream/uploads/videos/original";
    private static final long MAX_FILE_SIZE = 524288000; // 500 MB

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }
        request.getRequestDispatcher("/jsp/uploadVideo.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String token = JwtSessionHelper.getToken(session);
        String uploader = JwtSessionHelper.getUsername(session);

        try {
            // Get form parameters
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            Part videoPart = request.getPart("videoFile");

            // Validate inputs
            if (title == null || title.trim().isEmpty()) {
                redirectWithError(response, request, "Title is required");
                return;
            }

            if (videoPart == null || videoPart.getSize() == 0) {
                redirectWithError(response, request, "Please select a video file");
                return;
            }

            // Validate file size
            if (videoPart.getSize() > MAX_FILE_SIZE) {
                redirectWithError(response, request, "File size exceeds 500MB limit");
                return;
            }

            // Validate file type
            String contentType = videoPart.getContentType();
            if (!isValidVideoType(contentType)) {
                redirectWithError(response, request, "Only MP4 and WebM files are allowed");
                return;
            }

            // Get original filename
            String originalFilename = getFileName(videoPart);
            String extension = getFileExtension(originalFilename);

            // Generate unique filename (sanitize extension)
            String safeExtension = extension.replaceAll("[^a-zA-Z0-9._-]", "");
            String uniqueFilename = UUID.randomUUID().toString() + safeExtension;

            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save file to disk
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            try (InputStream fileContent = videoPart.getInputStream()) {
                Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Get video duration (optional, set to null for now)
            // In production, you'd use FFmpeg to extract this
            String duration = request.getParameter("duration");

            // Register video metadata in database via REST API
            Map<String, String> params = new HashMap<>();
            params.put("title", title);
            params.put("description", description != null ? description : "");
            params.put("filename", uniqueFilename);
            params.put("filePath", UPLOAD_DIR + File.separator + uniqueFilename);
            if (duration != null && !duration.trim().isEmpty()) {
                params.put("duration", duration);
            }

            RestClient.RestResponse restResponse = RestClient.postForm("/video/register", params, token);

            if (restResponse.statusCode() != 201) {
                // Registration failed, delete uploaded file
                Files.deleteIfExists(filePath);
                String errorMsg = JsonParser.extractValue(restResponse.body(), "message");
                redirectWithError(response, request, "Failed to register video: " + errorMsg);
                return;
            }

            // Extract video ID from response
            String videoId = JsonParser.extractValue(restResponse.body(), "id");
            
            // Redirect to video list with video ID for status polling
            if (videoId != null && !videoId.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/listVideo?uploading=" + videoId);
            } else {
                response.sendRedirect(request.getContextPath() + "/listVideo");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectWithError(response, request, "Upload failed: " + e.getMessage());
        }
    }

    private boolean isValidVideoType(String contentType) {
        if (contentType == null) return false;
        return contentType.equals("video/mp4") ||
               contentType.equals("video/webm") ||
               contentType.equals("video/x-msvideo"); // AVI
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "video.mp4";
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return ".mp4";
    }

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request,
                                   String errorMessage) throws IOException {
        response.sendRedirect(request.getContextPath() +
            "/uploadVideo?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
    }
}
