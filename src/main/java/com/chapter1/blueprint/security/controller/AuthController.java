package com.chapter1.blueprint.security.controller;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.security.dto.AuthDTO;
import com.chapter1.blueprint.security.util.JwtProcessor;
import com.chapter1.blueprint.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtProcessor jwtProcessor;
    private final MemberRepository memberRepository;

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDTO> refreshAccessToken(@RequestBody AuthDTO authDTO) {

        String refreshToken = authDTO.getRefreshToken();

        String memberId = jwtProcessor.getSubject(refreshToken);

        if (jwtProcessor.validateRefreshToken(refreshToken)) {
            Member member = memberRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new RuntimeException("Invalid User"));

            String newAccessToken = jwtProcessor.generateAccessToken(
                    member.getMemberId(),
                    member.getUid(),
                    member.getAuth(),
                    member.getMemberName(),
                    member.getEmail()
            );

            AuthDTO authResult = AuthDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .uid(member.getUid())
                    .memberId(member.getMemberId())
                    .memberName(member.getMemberName())
                    .email(member.getEmail())
                    .auth(member.getAuth())
                    .build();

            return ResponseEntity.ok(authResult);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
