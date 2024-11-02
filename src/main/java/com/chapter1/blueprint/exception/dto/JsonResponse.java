package com.chapter1.blueprint.exception.dto;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonResponse {

    private final ObjectMapper objectMapper;

    // 성공 응답을 보내는 메서드
    public <T> void sendSuccess(HttpServletResponse response, T data) throws IOException {
        SuccessResponse successResponse = new SuccessResponse(data);
        send(response, HttpStatus.OK, successResponse);
    }

    // 필드 에러가 없는 일반 에러 응답을 보내는 메서드
    public void sendError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        send(response, errorCode.getStatus(), errorResponse);
    }

    // 단일 필드 에러를 포함한 에러 응답을 보내는 메서드
    public void sendFieldError(HttpServletResponse response, ErrorCode errorCode, String field, String value, String reason) throws IOException {
        List<ErrorResponse.FieldError> fieldErrors = ErrorResponse.FieldError.of(field, value, reason);
        ErrorResponse errorResponse = new ErrorResponse(errorCode, fieldErrors);
        send(response, errorCode.getStatus(), errorResponse);
    }

    // 여러 필드 에러를 포함한 에러 응답을 보내는 메서드
    public void sendBindingErrors(HttpServletResponse response, ErrorCode errorCode, BindingResult bindingResult) throws IOException {
        List<ErrorResponse.FieldError> fieldErrors = ErrorResponse.FieldError.of(bindingResult);
        ErrorResponse errorResponse = new ErrorResponse(errorCode, fieldErrors);
        send(response, errorCode.getStatus(), errorResponse);
    }

    // HttpServletResponse에 JSON 응답을 작성하는 공통 메서드
    private void send(HttpServletResponse response, HttpStatus status, Object result) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Writer out = response.getWriter();
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
    }
}
