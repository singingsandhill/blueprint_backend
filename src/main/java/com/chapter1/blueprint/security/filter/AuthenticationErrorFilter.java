package com.chapter1.blueprint.security.filter;

import com.chapter1.blueprint.util.JsonResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationErrorFilter extends OncePerRequestFilter {

    private final JsonResponseUtil jsonResponseUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            jsonResponseUtil.sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    "Unauthorized access attempt. Reason: " + e.getMessage(),
                    "Unauthorized access due to invalid token.");
        }
    }
}
