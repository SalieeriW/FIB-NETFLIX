<%@page import="util.JwtSessionHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String from = request.getParameter("from");
    String errorMsg = request.getParameter("msg");

    if (errorMsg == null || errorMsg.trim().isEmpty()) {
        if ("login".equals(from)) {
            errorMsg = "Invalid username or password. Please try again.";
        } else {
            errorMsg = "An error occurred while processing your request.";
        }
    }

    boolean hasSession = JwtSessionHelper.isLoggedIn(session);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - VidStream</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <a href="<%= request.getContextPath() %>/listVideo" class="back-button" aria-label="Back to library">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
    </a>
    <jsp:include page="navbar.jsp" />
    <div class="main-content">
        <div class="container container-narrow">
            <h1>Error</h1>

    <div class="alert alert-error">
        <h2>Error</h2>
        <p><%= errorMsg %></p>
    </div>

            <div class="form-actions" style="margin-top: var(--space-8);">
                <% if ("login".equals(from) || !hasSession) { %>
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/jsp/login.jsp">Back to Login</a>
                <% } else { %>
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/listVideo">Back to Library</a>
                <a class="btn btn-secondary" href="<%= request.getContextPath() %>/jsp/login.jsp">Sign Out</a>
                <% } %>
            </div>

            <div class="text-center mt-8">
                <p class="text-muted" style="font-size: 0.875rem;">If the problem persists, please contact the administrator.</p>
            </div>
        </div>
    </div>
</body>
</html>
