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

@WebServlet("/createCourse")
public class createCourse extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        // Load videos for dropdown
        String token = JwtSessionHelper.getToken(session);
        try {
            RestClient.RestResponse restResponse = RestClient.get("/video/list", token);
            if (restResponse.statusCode() == 200) {
                request.setAttribute("videosJson", restResponse.body());
            }
        } catch (Exception e) {
            // Continue without videos if there's an error
            request.setAttribute("videosJson", "[]");
        }

        request.getRequestDispatcher("/jsp/createCourse.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String title = request.getParameter("title");
        String language = request.getParameter("language");
        String videoId = request.getParameter("videoId");

        if (title == null || title.trim().isEmpty()) {
            request.setAttribute("error", "Title is required");
            request.getRequestDispatcher("/jsp/createCourse.jsp").forward(request, response);
            return;
        }

        String token = JwtSessionHelper.getToken(session);
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        if (language != null && !language.trim().isEmpty()) {
            params.put("language", language);
        }
        if (videoId != null && !videoId.trim().isEmpty()) {
            params.put("videoId", videoId);
        }

        try {
            RestClient.RestResponse restResponse = RestClient.postForm("/course/create", params, token);

            if (restResponse.statusCode() == 201) {
                response.sendRedirect(request.getContextPath() + "/listCourses?success=Course created successfully");
            } else {
                String errorMsg = "Failed to create course";
                try {
                    Map<String, String> errorJson = JsonParser.parseJsonObject(restResponse.body());
                    if (errorJson.containsKey("message")) {
                        errorMsg = errorJson.get("message");
                    }
                } catch (Exception e) {
                    // Use default error message
                }
                request.setAttribute("error", errorMsg);
                request.getRequestDispatcher("/jsp/createCourse.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Error creating course: " + e.getMessage());
            request.getRequestDispatcher("/jsp/createCourse.jsp").forward(request, response);
        }
    }
}

