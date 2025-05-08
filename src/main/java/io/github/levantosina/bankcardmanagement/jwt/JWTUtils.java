package io.github.levantosina.bankcardmanagement.jwt;

import jakarta.servlet.http.HttpServletRequest;

public class JWTUtils {

    private static final String BEARER_PREFIX = "Bearer ";
    public static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
