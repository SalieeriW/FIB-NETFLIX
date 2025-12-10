package util;

import jakarta.servlet.http.HttpSession;

/**
 * Helper class for managing JWT tokens in HttpSession
 */
public class JwtSessionHelper {

    private static final String JWT_TOKEN_KEY = "jwtToken";
    private static final String USERNAME_KEY = "username";

    /**
     * Checks if the user is logged in by verifying JWT token exists in session
     * @param session HttpSession to check
     * @return true if logged in (token present), false otherwise
     */
    public static boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }
        String token = (String) session.getAttribute(JWT_TOKEN_KEY);
        return token != null && !token.isEmpty();
    }

    /**
     * Gets the JWT token from session
     * @param session HttpSession to retrieve token from
     * @return JWT token string or null if not present
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(JWT_TOKEN_KEY);
    }

    /**
     * Gets the username from session
     * @param session HttpSession to retrieve username from
     * @return Username string or null if not present
     */
    public static String getUsername(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(USERNAME_KEY);
    }

    /**
     * Stores JWT token and username in session
     * @param session HttpSession to store in
     * @param token JWT token string
     * @param username Username string
     */
    public static void login(HttpSession session, String token, String username) {
        if (session != null) {
            session.setAttribute(JWT_TOKEN_KEY, token);
            session.setAttribute(USERNAME_KEY, username);
        }
    }

    /**
     * Clears JWT token and username from session
     * @param session HttpSession to clear
     */
    public static void logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(JWT_TOKEN_KEY);
            session.removeAttribute(USERNAME_KEY);
            session.invalidate();
        }
    }
}
