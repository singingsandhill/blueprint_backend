package com.chapter1.blueprint.exception.dto;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse {
    private final Boolean success = false;
    private final int status;           // 에러 상태 코드
    private final String code;          // 에러 코드
    private final String message;       // 에러 메시지
    private final List<FieldError> errors;  // 필드 에러 상세 메시지

    // 필드 에러가 없는 경우
    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = List.of(); // 빈 리스트로 초기화
    }

    // 필드 에러가 있는 경우
    public ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }

    @Getter
    @RequiredArgsConstructor
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        // 단일 필드 에러 생성
        public static List<FieldError> of(String field, String value, String reason) {
            return List.of(new FieldError(field, value, reason));
        }

        // BindingResult를 통해 여러 필드 에러 생성
        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .collect(Collectors.toList());
        }
    }
}
