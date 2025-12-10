package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.JwtSessionHelper;
import util.RestClient;
import util.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/deleteVideo")
public class deleteVideo extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads/videos/original";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String token = JwtSessionHelper.getToken(session);
        String currentUser = JwtSessionHelper.getUsername(session);

        try {
            String videoId = request.getParameter("id");

            if (videoId == null || videoId.trim().isEmpty()) {
                redirectWithError(response, request, "Video ID is required");
                return;
            }

            // First, get video details to check ownership and get file path
            RestClient.RestResponse getResponse = RestClient.get("/video/search/" + videoId, token);

            if (getResponse.statusCode() != 200) {
                String errorMsg = JsonParser.extractValue(getResponse.body(), "message");
                redirectWithError(response, request, "Video not found: " + errorMsg);
                return;
            }

            // Parse video to check ownership
            String videoJson = getResponse.body();
            String uploader = JsonParser.extractValue(videoJson, "uploader");
            String filePath = JsonParser.extractValue(videoJson, "filePath");
            String processedPath = JsonParser.extractValue(videoJson, "processedPath");
            String status = JsonParser.extractValue(videoJson, "status");

            // Verify ownership
            if (!currentUser.equals(uploader)) {
                redirectWithError(response, request, "You can only delete your own videos");
                return;
            }

            // Note: Videos can be deleted even while processing
            // The transcoding process will continue but the video entry will be removed

            // Call REST API to delete video from database
            Map<String, String> params = new HashMap<>();
            params.put("id", videoId);

            RestClient.RestResponse deleteResponse = RestClient.postForm("/video/delete", params, token);

            if (deleteResponse.statusCode() != 200) {
                String errorMsg = JsonParser.extractValue(deleteResponse.body(), "message");
                redirectWithError(response, request, "Failed to delete video: " + errorMsg);
                return;
            }

            // Delete physical files if they exist
            // Delete original uploaded file
            if (filePath != null && !filePath.trim().isEmpty()) {
                try {
                    Path fileToDelete = Paths.get(filePath);
                    Files.deleteIfExists(fileToDelete);
                } catch (IOException e) {
                    // Log error but don't fail the request since DB entry is already deleted
                    System.err.println("Error deleting original file: " + e.getMessage());
                }
            }
            
            // Delete processed files directory if it exists
            if (processedPath != null && !processedPath.trim().isEmpty()) {
                try {
                    Path processedDir = Paths.get(processedPath);
                    if (Files.exists(processedDir) && Files.isDirectory(processedDir)) {
                        // Delete all files in the processed directory
                        Files.walk(processedDir)
                            .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    System.err.println("Error deleting processed file: " + path + " - " + e.getMessage());
                                }
                            });
                    }
                } catch (IOException e) {
                    // Log error but don't fail the request
                    System.err.println("Error deleting processed directory: " + e.getMessage());
                }
            }

            // Success
            response.sendRedirect(request.getContextPath() +
                "/listVideo?success=" + java.net.URLEncoder.encode("Video deleted successfully", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            redirectWithError(response, request, "Delete failed: " + e.getMessage());
        }
    }

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request,
                                   String errorMessage) throws IOException {
        response.sendRedirect(request.getContextPath() +
            "/listVideo?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
    }
}
