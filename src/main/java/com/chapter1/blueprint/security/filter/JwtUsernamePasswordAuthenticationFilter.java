package com.chapter1.blueprint.security.filter;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.security.dto.LoginDTO;
import com.chapter1.blueprint.security.handle.LoginFailureHandler;
import com.chapter1.blueprint.security.handle.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final MemberRepository memberRepository;
    private final LoginFailureHandler loginFailureHandler;
    private final ObjectMapper objectMapper;

    public JwtUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            MemberRepository memberRepository,
            ObjectMapper objectMapper) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
        this.loginFailureHandler = loginFailureHandler;
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/member/login");
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // LoginDTO null 체크
            LoginDTO login = LoginDTO.of(request, objectMapper);
            if (login == null || login.getMemberId() == null) {
                throw new UsernameNotFoundException("로그인 정보가 올바르지 않습니다.");
            }

            // 로그인 시도 횟수 체크
            int attempts = loginFailureHandler.getAttemptsCache().getOrDefault(login.getMemberId(), 0);
            if (attempts >= LoginFailureHandler.getMAX_ATTEMPTS()) {
                throw new BadCredentialsException("로그인 시도가 초과되었습니다.");
            }

            // Member 조회 및 null 체크
            Member member = memberRepository.findByMemberId(login.getMemberId())
                    .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));

            // Member 로그인 잠금 상태 체크
            if (member.getIsLoginLocked() != null && member.getIsLoginLocked()) {
                throw new BadCredentialsException("계정이 잠겨있습니다. 관리자에게 문의하세요.");
            }

            // 요청 속성 설정
            request.setAttribute("memberId", member.getMemberId());

            // 디버그 로깅
            log.debug("Attempting authentication for ID: {}", login.getMemberId());

            // 인증 토큰 생성 및 인증 시도
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(login.getMemberId(), login.getPassword());

            return getAuthenticationManager().authenticate(authenticationToken);

        } catch (Exception e) {
            log.error("Authentication attempt failed", e);
            throw new AuthenticationException("인증 처리 중 오류가 발생했습니다: " + e.getMessage()) {};
        }
    }
}
