package com.chapter1.blueprint.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthDTO {
    private Long uid;
    private String memberId;
    private String memberName;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String auth;
}
