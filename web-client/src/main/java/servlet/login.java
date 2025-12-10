package servlet;

import util.JsonParser;
import util.JwtSessionHelper;
import util.RestClient;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "login", urlPatterns = {"/login"})
public class login extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
            return;
        }
        request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> params = new HashMap<>();
        params.put("username", username != null ? username : "");
        params.put("password", password != null ? password : "");

        try {
            RestClient.RestResponse restResponse = RestClient.postForm("/login", params);
            if (restResponse.statusCode() == HttpServletResponse.SC_OK) {
                String token = JsonParser.extractString(restResponse.body(), "token");
                String returnedUsername = JsonParser.extractString(restResponse.body(), "username");

                if (token != null && !token.isEmpty()) {
                    HttpSession session = request.getSession(true);
                    JwtSessionHelper.login(session, token, returnedUsername);
                    response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
                } else {
                    response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?from=login");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?from=login");
            }
        } catch (Exception e) {
            System.err.println("REST login error: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?from=login");
        }
    }

    @Override
    public String getServletInfo() {
        return "Login servlet delegating authentication to REST service";
    }
}
