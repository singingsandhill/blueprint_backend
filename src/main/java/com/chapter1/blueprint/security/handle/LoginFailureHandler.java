package com.chapter1.blueprint.security.handle;

import com.chapter1.blueprint.exception.util.JsonResponseUtil;
import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final MemberRepository memberRepository;
    private final JsonResponseUtil jsonResponseUtil;

    @Getter
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 3 * 60 * 1000; // 3분

    @Getter
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String memberId = request.getParameter("memberId");
        Member member = memberRepository.findByMemberId(memberId)
                .orElse(null);

        if (exception instanceof BadCredentialsException && member == null) {
            jsonResponseUtil.sendErrorResponse(response, HttpStatus.NOT_FOUND, "Member ID not found", "Member ID not found");
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (Boolean.TRUE.equals(member.getIsLoginLocked())) {
            if (currentTime < member.getLoginLockTime() + LOCK_TIME_DURATION) {
                jsonResponseUtil.sendErrorResponse(response, HttpStatus.LOCKED, "Account is temporarily locked due to too many failed attempts.", "Account is temporarily locked. Try again later.");
                return;
            } else {
                member.setIsLoginLocked(false);
                member.setLoginLockTime(null);
                memberRepository.save(member);
                attemptsCache.put(memberId, 0);
            }
        }

        int attempts = attemptsCache.getOrDefault(memberId, 0) + 1;
        attemptsCache.put(memberId, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            member.setIsLoginLocked(true);
            member.setLoginLockTime(currentTime);
            memberRepository.save(member);
            jsonResponseUtil.sendErrorResponse(response, HttpStatus.LOCKED, "Account locked due to too many failed login attempts.", "Account is temporarily locked.");
        } else {
            jsonResponseUtil.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid password.", "Invalid credentials.");
        }
    }
}
