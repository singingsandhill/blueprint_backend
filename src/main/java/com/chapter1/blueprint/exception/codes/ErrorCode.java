package com.chapter1.blueprint.exception.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통(Common)
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "COMMON_001", "토큰이 유효하지 않습니다."),
    AUTHORIZATION_DENIED(HttpStatus.UNAUTHORIZED, "COMMON_002", "권한이 부족합니다."),

    // 회원(Member)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "존재하지 않는 회원입니다."),

    // 서버(Server)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부 에러가 발생했습니다."),

    // 요청(Request 관련 에러
    BAD_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "G001", "잘못된 요청입니다."),

    // 이메일(Email)
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "EMAIL_001", "유효하지 않은 인증 코드입니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_002", "인증 코드가 만료되었습니다."),
    EMAIL_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_003", "이메일 전송에 실패하였습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
