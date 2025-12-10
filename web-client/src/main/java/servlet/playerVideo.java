package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Video;
import util.JwtSessionHelper;
import util.RestClient;
import util.JsonParser;

import java.io.IOException;
import java.util.HashMap;

@WebServlet("/playerVideo")
public class playerVideo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String token = JwtSessionHelper.getToken(session);
        String videoId = request.getParameter("id");

        if (videoId == null || videoId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/listVideo?error=Video+ID+required");
            return;
        }

        try {
            RestClient.RestResponse restResponse = RestClient.get("/video/search/" + videoId, token);

            if (restResponse.statusCode() == 200) {
                Video video = JsonParser.parseVideo(restResponse.body());
                request.setAttribute("video", video);

                RestClient.RestResponse viewsResponse = RestClient.postForm("/video/incrementViews/" + videoId, new HashMap<>(), token);

                if (viewsResponse.statusCode() == 200) {
                    Video updatedVideo = JsonParser.parseVideo(viewsResponse.body());
                    request.setAttribute("video", updatedVideo);
                }

            } else {
                String errorMsg = JsonParser.extractValue(restResponse.body(), "message");
                request.setAttribute("error", "Failed to load video: " + errorMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading video: " + e.getMessage());
        }

        request.getRequestDispatcher("/jsp/player.jsp").forward(request, response);
    }
}
