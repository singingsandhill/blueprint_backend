package com.chapter1.blueprint.security.handle;

import com.chapter1.blueprint.exception.dto.JsonResponse;
import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.security.dto.AuthDTO;
import com.chapter1.blueprint.security.util.JwtProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProcessor jwtProcessor;
    private final MemberRepository memberRepository;
    private final JsonResponse jsonResponse;

    private AuthDTO makeAuth(Member member) {
        Long uid = member.getUid();
        String memberId = member.getMemberId();
        String memberName = member.getMemberName();
        String email = member.getEmail();
        String auth = member.getAuth();

        String accessToken = jwtProcessor.generateAccessToken(memberId, uid, auth, memberName, email);
        String refreshToken = jwtProcessor.generateRefreshToken(memberId);

        return AuthDTO.builder()
                .uid(uid)
                .memberId(memberId)
                .memberName(memberName)
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .auth(auth)
                .build();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUsername();

        Optional<Member> memberOptional = memberRepository.findByMemberId(memberId);
        if (memberOptional.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않는 사용자입니다.");
            return;
        }

        Member member = memberOptional.get();

        AuthDTO result = makeAuth(member);
        jsonResponse.sendSuccess(response, result);

        resetLoginFailures(member);
    }

    private void resetLoginFailures(Member member) {
        member.setIsLoginLocked(false);
        member.setLoginLockTime(null);
        memberRepository.save(member);
    }
}
