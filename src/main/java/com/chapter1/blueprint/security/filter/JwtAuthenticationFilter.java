package com.chapter1.blueprint.security.filter;

import com.chapter1.blueprint.security.util.JwtProcessor;
import com.chapter1.blueprint.util.JsonResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProcessor jwtProcessor;
    private final JsonResponseUtil jsonResponseUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProcessor.validateToken(token)) {
                var authentication = jwtProcessor.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                jsonResponseUtil.sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                        "Invalid or expired token in authorization header.",
                        "The provided token is invalid or expired.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
