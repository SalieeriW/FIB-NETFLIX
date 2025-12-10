<%@page import="util.JwtSessionHelper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession currentSession = request.getSession(false);
    if (currentSession != null && JwtSessionHelper.isLoggedIn(currentSession)) {
        response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - VidStream</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <script src="<%= request.getContextPath() %>/js/toast.js"></script>
</head>
<body>
    <a href="<%= request.getContextPath() %>/listVideo" class="back-button" aria-label="Back to library">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
    </a>
<div class="hero-section" style="min-height: 100vh; margin: 0;">
    <div class="container container-narrow">
        <div class="text-center">
            <h1 class="hero-title" style="color: var(--accent); font-size: 2.5rem; margin-bottom: var(--space-6); font-weight: 700;">VIDSTREAM</h1>
            <h2 style="font-size: 1.75rem; font-weight: 600; margin-bottom: var(--space-4);">Sign Up</h2>
        </div>

    <%
        String error = (String) request.getAttribute("error");
        String username = (String) request.getAttribute("username");
        if (username == null) username = "";
    %>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            <% if (error != null) { %>
                createToast('<%= error.replace("'", "\\'") %>', 'error');
            <% } %>
        });
    </script>

    <form method="post" action="<%= request.getContextPath() %>/registrarUsuario">
        <div class="form-group">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username"
                   value="<%= username %>"
                   placeholder="Letters and numbers only, 3-20 characters"
                   required>
        </div>

        <div class="form-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password"
                   placeholder="At least 4 characters"
                   required>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Confirm Password:</label>
            <input type="password" id="confirmPassword" name="confirmPassword"
                   placeholder="Re-enter your password"
                   required>
        </div>

        <div class="form-group form-actions">
            <input type="submit" value="Create Account" class="btn btn-primary">
            <a href="<%= request.getContextPath() %>/jsp/login.jsp" class="btn btn-secondary">Back to Login</a>
        </div>
    </form>

        <div class="text-center mt-8">
            <p class="text-muted">Already have an account? 
                <a href="<%= request.getContextPath() %>/jsp/login.jsp" style="color: var(--netflix-red); font-weight: 600;">Sign in</a>
            </p>
        </div>
    </div>
</div>
</body>
</html>
