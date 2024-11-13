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

        LoginDTO login = LoginDTO.of(request, objectMapper);
        log.info("login: {}", login);
        log.info("login getMemberId: {}", login.getMemberId());
        Member member = memberRepository.findByMemberId(login.getMemberId())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));

        request.setAttribute("memberId", member.getMemberId());
        log.info("request getMemberId: {}", login.getMemberId());

        int attempts = loginFailureHandler.getAttemptsCache().getOrDefault(login.getMemberId(), 0);
        if (attempts >= LoginFailureHandler.getMAX_ATTEMPTS()) {
            throw new BadCredentialsException("로그인 시도가 초과되었습니다.");
        }

        log.info("Attempting authentication for ID: {}", login.getMemberId());
        log.info("Password: {}", login.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(login.getMemberId(), login.getPassword());



        return getAuthenticationManager().authenticate(authenticationToken);
    }
}
