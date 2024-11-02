package com.chapter1.blueprint.global.DTO;

import lombok.Getter;

@Getter
public class ResponseDTO {
    private final Boolean success;
    private final DataDTO response;

    public ResponseDTO(Boolean success, Object data) {
        this.success = success;
        this.response = new DataDTO(data);
    }
}
