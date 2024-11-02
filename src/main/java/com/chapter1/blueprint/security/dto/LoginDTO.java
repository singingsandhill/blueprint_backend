package com.chapter1.blueprint.security.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginDTO {

    private String memberId;
    private String password;
    private String accessToken;
    private String refreshToken;

    public static LoginDTO of(HttpServletRequest request, ObjectMapper objectMapper) throws AuthenticationException {
        try {
            return objectMapper.readValue(request.getInputStream(), LoginDTO.class);
        } catch (IOException e) {
            throw new BadCredentialsException("memberId 또는 password가 없습니다.");
        }
    }
}
