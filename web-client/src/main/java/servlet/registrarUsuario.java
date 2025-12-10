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

@WebServlet(name = "registrarUsuario", urlPatterns = {"/registrarUsuario"})
public class registrarUsuario extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
            return;
        }
        request.getRequestDispatcher("/jsp/registrarUsuario.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        Map<String, String> params = new HashMap<>();
        params.put("username", username != null ? username : "");
        params.put("password", password != null ? password : "");
        params.put("confirmPassword", confirm != null ? confirm : "");

        try {
            RestClient.RestResponse restResponse = RestClient.postForm("/registerUser", params);
            if (restResponse.statusCode() == HttpServletResponse.SC_OK) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?registered=true");
            } else {
                String message = JsonParser.extractValue(restResponse.body(), "message");
                request.setAttribute("error", message != null ? message : "Registration failed");
                request.setAttribute("username", username);
                request.getRequestDispatcher("/jsp/registrarUsuario.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.err.println("User registration failed via REST: " + e.getMessage());
            request.setAttribute("error", "Unexpected error. Please try again later.");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/jsp/registrarUsuario.jsp").forward(request, response);
        }
    }
}
