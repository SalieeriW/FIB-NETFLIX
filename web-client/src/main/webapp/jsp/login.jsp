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
    <title>Login - VidStream</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/toast.js"></script>
</head>
<body>
    <a href="${pageContext.request.contextPath}/listVideo" class="back-button" aria-label="Back to library">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
    </a>
<div class="hero-section" style="min-height: 100vh; margin: 0;">
    <div class="container container-narrow">
        <div class="text-center">
            <h1 class="hero-title" style="color: var(--accent); font-size: 2.5rem; margin-bottom: var(--space-6); font-weight: 700;">VIDSTREAM</h1>
            <h2 style="font-size: 1.75rem; font-weight: 600; margin-bottom: var(--space-4);">Sign In</h2>
        </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            <% String registered = request.getParameter("registered"); %>
            <% if ("true".equals(registered)) { %>
                createToast('Account created successfully. Please login with your credentials.', 'success');
            <% } %>
        });
    </script>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="form-group">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" required>
        </div>

        <div class="form-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
        </div>

        <div class="form-group form-actions">
            <input type="submit" value="Login" class="btn btn-primary">
            <a href="${pageContext.request.contextPath}/jsp/registrarUsuario.jsp" class="btn btn-secondary">Create Account</a>
        </div>
    </form>

        <div class="text-center mt-8">
            <p class="text-muted">Don't have an account? 
                <a href="${pageContext.request.contextPath}/jsp/registrarUsuario.jsp" style="color: var(--netflix-red); font-weight: 600;">Sign up</a>
            </p>
        </div>
    </div>
</div>
</body>
</html>
