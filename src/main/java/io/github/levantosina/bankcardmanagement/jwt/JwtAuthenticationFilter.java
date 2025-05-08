package io.github.levantosina.bankcardmanagement.jwt;

import io.github.levantosina.bankcardmanagement.service.OwnUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final OwnUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();


        if (servletPath.equals("/api/v1/auth/login") || servletPath.equals("/api/v1/card-management")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = JWTUtils.extractToken(request);

        if (token == null) {

            log.info("No JWT token found, skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        final String username = jwtUtil.extractUsername(token);


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );


                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("User {} authenticated successfully.", username);
                }
            } catch (Exception e) {
                log.error("Authentication failed for token {}: {}", token, e.getMessage());
            }
        } else {
            log.warn("JWT token is either expired or invalid for user: {}", username);
        }


        filterChain.doFilter(request, response);
    }
}