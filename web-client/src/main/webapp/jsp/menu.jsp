<%@page import="util.JwtSessionHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Redirect to video library instead of showing menu
    if (JwtSessionHelper.isLoggedIn(session)) {
        response.sendRedirect(request.getContextPath() + "/listVideo");
    } else {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
    }
%>
