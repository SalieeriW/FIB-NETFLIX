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
import java.util.List;

@WebServlet("/listVideo")
public class listVideo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String token = JwtSessionHelper.getToken(session);
        String currentUser = JwtSessionHelper.getUsername(session);

        String searchQuery = request.getParameter("q");

        try {
            RestClient.RestResponse restResponse;

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                restResponse = RestClient.get("/video/searchQuery/" + java.net.URLEncoder.encode(searchQuery, "UTF-8"), token);
            } else {
                restResponse = RestClient.get("/video/list", token);
            }

            if (restResponse.statusCode() == 200) {
                List<Video> videos = JsonParser.parseVideoArray(restResponse.body());
                request.setAttribute("videos", videos);
                request.setAttribute("currentUser", currentUser);
                request.setAttribute("searchQuery", searchQuery);
            } else {
                String errorMsg = JsonParser.extractValue(restResponse.body(), "message");
                request.setAttribute("error", "Failed to load videos: " + errorMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading videos: " + e.getMessage());
        }

        request.getRequestDispatcher("/jsp/listVideo.jsp").forward(request, response);
    }
}
