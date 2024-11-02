package com.chapter1.blueprint.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SuccessResponse {
    private final Boolean success = true;
    private final DataDTO response;

    public SuccessResponse(Object data) {
        this.response = new DataDTO(data);
    }
}

