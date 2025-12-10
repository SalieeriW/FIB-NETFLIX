package security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonSerializer;

import java.io.IOException;

@WebFilter(urlPatterns = {"/resources/*"})
public class JwtFilter implements Filter {

    private static final String[] EXCLUDED_PATHS = {
        "/resources/login",
        "/resources/registerUser",
        "/resources/video/stream/",
        "/resources/video/segment/",
        "/resources/video/list",
        "/resources/video/search/",
        "/resources/video/searchTitle/",
        "/resources/video/searchUploader/",
        "/resources/video/searchQuery/",
        "/resources/video/status/",
        "/resources/video/thumbnail/"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Skip JWT validation for excluded paths
        if (isExcludedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(httpResponse, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            Claims claims = JwtProvider.validateToken(token);
            String username = claims.getSubject();

            // Store username in request attribute for use in endpoints
            httpRequest.setAttribute("jwtUser", username);

            chain.doFilter(request, response);
        } catch (JwtException e) {
            sendUnauthorized(httpResponse, "Invalid or expired token: " + e.getMessage());
        }
    }

    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.contains(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String errorJson = JsonSerializer.errorResponse("Unauthorized", message, 401);
        response.getWriter().write(errorJson);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
