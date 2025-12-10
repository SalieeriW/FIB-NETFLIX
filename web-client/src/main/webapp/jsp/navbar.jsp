<%@page import="util.JwtSessionHelper"%>
<%
    String username = null;
    if (JwtSessionHelper.isLoggedIn(session)) {
        username = JwtSessionHelper.getUsername(session);
    }
%>
<nav class="navbar">
    <div class="navbar-content">
        <a href="<%= username != null ? request.getContextPath() + "/listVideo" : request.getContextPath() + "/jsp/login.jsp" %>" 
           class="navbar-brand">VIDSTREAM</a>
        <% if (username != null) { %>
            <div class="navbar-links">
                <a href="<%= request.getContextPath() %>/listVideo" class="navbar-link">
                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                        <polyline points="9 22 9 12 15 12 15 22"></polyline>
                    </svg>
                    Home
                </a>
                <a href="<%= request.getContextPath() %>/uploadVideo" class="navbar-link">
                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="17 8 12 3 7 8"></polyline>
                        <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    Upload
                </a>
                <a href="<%= request.getContextPath() %>/listCourses" class="navbar-link">
                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                    </svg>
                    Courses
                </a>
                <span class="navbar-user"><%= username %></span>
                <form action="<%= request.getContextPath() %>/logout" method="POST" style="display: inline;">
                    <button type="submit" class="btn btn-secondary" style="padding: var(--space-2) var(--space-3); font-size: 0.875rem;">
                        <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                            <polyline points="16 17 21 12 16 7"></polyline>
                            <line x1="21" y1="12" x2="9" y2="12"></line>
                        </svg>
                        Sign Out
                    </button>
                </form>
            </div>
        <% } else { %>
            <div class="navbar-links">
                <a href="<%= request.getContextPath() %>/jsp/login.jsp" class="navbar-link">
                    <svg class="icon icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                        <polyline points="10 17 15 12 10 7"></polyline>
                        <line x1="15" y1="12" x2="3" y2="12"></line>
                    </svg>
                    Sign In
                </a>
            </div>
        <% } %>
    </div>
</nav>

