package com.chapter1.blueprint.member.domain.dto;

import lombok.Data;

@Data
public class FindPasswordDTO {
    private String memberId;
    private String email;
}
