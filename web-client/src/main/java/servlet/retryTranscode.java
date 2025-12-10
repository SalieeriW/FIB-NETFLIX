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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/retryTranscode")
public class retryTranscode extends HttpServlet {

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

            RestClient.RestResponse getResponse = RestClient.get("/video/search/" + videoId, token);

            if (getResponse.statusCode() != 200) {
                String errorMsg = JsonParser.extractValue(getResponse.body(), "message");
                redirectWithError(response, request, "Video not found: " + errorMsg);
                return;
            }

            String videoJson = getResponse.body();
            String uploader = JsonParser.extractValue(videoJson, "uploader");
            String status = JsonParser.extractValue(videoJson, "status");
            String filePath = JsonParser.extractValue(videoJson, "filePath");

            if (!currentUser.equals(uploader)) {
                redirectWithError(response, request, "You can only retry your own videos");
                return;
            }

            if (!"ERROR".equals(status)) {
                redirectWithError(response, request, "Only ERROR videos can be retried");
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("id", videoId);
            params.put("status", "UPLOADING");

            RestClient.RestResponse updateResponse = RestClient.postForm("/video/updateStatus", params, token);

            if (updateResponse.statusCode() != 200) {
                String errorMsg = JsonParser.extractValue(updateResponse.body(), "message");
                redirectWithError(response, request, "Failed to retry: " + errorMsg);
                return;
            }

            response.sendRedirect(request.getContextPath() +
                "/listVideo?success=" + java.net.URLEncoder.encode("Transcoding retry initiated", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            redirectWithError(response, request, "Retry failed: " + e.getMessage());
        }
    }

    private void redirectWithError(HttpServletResponse response, HttpServletRequest request,
                                   String errorMessage) throws IOException {
        response.sendRedirect(request.getContextPath() +
            "/listVideo?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
    }
}
