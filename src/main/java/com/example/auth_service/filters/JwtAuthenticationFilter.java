package com.example.auth_service.filters;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auth_service.services.OAuth2Service;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
            throws jakarta.servlet.ServletException, IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println(String.format("Hit JwtAuthenticationFilter with auth header:: %s", authorizationHeader));

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            final String token = authorizationHeader.substring(7);
            if (OAuth2Service.validateAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing token");
    }
}
