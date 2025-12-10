package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.JwtSessionHelper;

import java.io.IOException;

@WebServlet("/chat")
public class chat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!JwtSessionHelper.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
            return;
        }

        String courseId = request.getParameter("id");
        if (courseId == null || courseId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/listCourses");
            return;
        }

        request.getRequestDispatcher("/jsp/chat.jsp").forward(request, response);
    }
}

