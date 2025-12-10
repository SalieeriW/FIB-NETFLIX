package security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtProvider {

    private static final String SECRET_KEY = System.getenv("JWT_SECRET") != null
        ? System.getenv("JWT_SECRET")
        : "practica3-secret-key-for-jwt-authentication-minimum-256-bits-required";

    private static final long EXPIRATION_TIME = 30 * 60 * 1000; // 30 minutes

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * Creates a JWT token for the given username
     * @param username The username to encode in the token
     * @return JWT token string
     */
    public static String createToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates and parses a JWT token
     * @param token The JWT token to validate
     * @return Claims object containing token data
     * @throws JwtException if token is invalid or expired
     */
    public static Claims validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts username from a JWT token
     * @param token The JWT token
     * @return Username from token subject
     * @throws JwtException if token is invalid
     */
    public static String getUsernameFromToken(String token) throws JwtException {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }
}
