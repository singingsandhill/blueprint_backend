package com.chapter1.blueprint.security.filter;

import com.chapter1.blueprint.exception.util.JsonResponseUtil;
import com.chapter1.blueprint.security.util.JwtProcessor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProcessor jwtProcessor;
    private final JsonResponseUtil jsonResponseUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. Authorization 헤더 추출
        String header = request.getHeader("Authorization");
        log.info("Authorization Header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            // 2. JWT 토큰 추출
            String token = header.substring(7);
            log.info("Extracted Token: {}", token);

            try {
                // 3. 토큰 검증
                if (jwtProcessor.validateToken(token)) {
                    log.info("Token is valid: {}", token);

                    // 4. 인증 객체 생성 및 SecurityContextHolder 설정
                    var authentication = jwtProcessor.getAuthentication(token);
                    if (authentication != null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Authentication set in SecurityContextHolder: {}", authentication);
                    } else {
                        log.warn("Failed to create authentication from token: {}", token);
                    }
                } else {
                    log.warn("Invalid or expired token: {}", token);
                    jsonResponseUtil.sendErrorResponse(
                            response,
                            HttpStatus.UNAUTHORIZED,
                            "Invalid or expired token in authorization header.",
                            "The provided token is invalid or expired."
                    );
                    return;
                }
            } catch (Exception e) {
                log.error("Error processing JWT token: {}", token, e);
                jsonResponseUtil.sendErrorResponse(
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "Error processing token.",
                        e.getMessage()
                );
                return;
            }
        } else {
            if (header == null) {
                log.warn("Authorization header is missing.");
            } else {
                log.warn("Authorization header does not start with 'Bearer '. Header: {}", header);
            }
        }

        // 5. 필터 체인으로 요청 전달
        filterChain.doFilter(request, response);
    }
}
