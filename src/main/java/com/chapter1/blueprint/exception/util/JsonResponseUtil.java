package com.chapter1.blueprint.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonResponseUtil {

    private final ObjectMapper objectMapper;

    // 인증 실패 또는 접근 거부에 대한 JSON 응답을 생성하고 로그를 남김
    public void sendErrorResponse(HttpServletResponse response, HttpStatus status, String logMessage, String clientMessage) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        log.error(logMessage);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", clientMessage);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
