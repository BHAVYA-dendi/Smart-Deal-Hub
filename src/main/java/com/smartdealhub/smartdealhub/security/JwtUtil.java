package com.smartdealhub.smartdealhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String DEFAULT_SECRET = "smartdealhub-default-jwt-secret-change-in-production-2026";
    private static final String RAW_SECRET = System.getenv().getOrDefault("JWT_SECRET", DEFAULT_SECRET);
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(RAW_SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; // 1 hour

    // Generate JWT token
    public static String generateToken(String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRATION_TIME_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateToken(String email) {
        return generateToken(email, "USER");
    }

    // Validate JWT token
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token); // throws exception if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract email from token
    public static String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object role = claims.get("role");
            return role != null ? role.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}