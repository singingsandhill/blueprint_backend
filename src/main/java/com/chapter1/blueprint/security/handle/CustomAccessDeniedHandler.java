package com.chapter1.blueprint.security.handle;

import com.chapter1.blueprint.util.JsonResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonResponseUtil jsonResponseUtil;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        jsonResponseUtil.sendErrorResponse(response, HttpStatus.FORBIDDEN,
                "Access denied. Reason: " + accessDeniedException.getMessage(),
                "You do not have permission to access this resource.");
    }
}
