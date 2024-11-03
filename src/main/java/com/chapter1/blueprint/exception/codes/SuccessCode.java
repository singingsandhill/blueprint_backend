package com.chapter1.blueprint.exception.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    SELECT_SUCCESS(HttpStatus.OK.value(), "200", "조회 성공"),
    DELETE_SUCCESS(HttpStatus.OK.value(), "200", "삭제 성공"),
    INSERT_SUCCESS(HttpStatus.CREATED.value(), "201", "삽입 성공"),
    UPDATE_SUCCESS(HttpStatus.NO_CONTENT.value(), "204", "수정 성공");

    private final int status;
    private final String code;
    private final String message;
}
