package com.chapter1.blueprint.security.config;

import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.security.filter.AuthenticationErrorFilter;
import com.chapter1.blueprint.security.filter.JwtAuthenticationFilter;
import com.chapter1.blueprint.security.filter.JwtUsernamePasswordAuthenticationFilter;
import com.chapter1.blueprint.security.handle.CustomAccessDeniedHandler;
import com.chapter1.blueprint.security.handle.CustomAuthenticationEntryPoint;
import com.chapter1.blueprint.security.handle.LoginFailureHandler;
import com.chapter1.blueprint.security.handle.LoginSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        AuthenticationManager authenticationManager = authenticationManager(authenticationConfiguration);

        JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter =
                new JwtUsernamePasswordAuthenticationFilter(
                        authenticationManager,
                        loginSuccessHandler,
                        loginFailureHandler,
                        memberRepository,
                        objectMapper
                );

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(
                                "/member/login",
                                "/member/register",
                                "/member/checkMemberId/**",
                                "/member/checkEmail/**",
                                "/member/find/memberId",
                                "/member/find/password",
                                "/member/email/sendVerification",
                                "/member/email/verifyEmailCode"
                        ).permitAll()
                        .requestMatchers("/member/**").authenticated()
                        .requestMatchers("/finance/filter/**").authenticated()
                        .requestMatchers("/policy/recommendation").authenticated()
                        .requestMatchers("/policy/list/**", "/policy/detail/**", "/policy/filter", "/policy/update/TK","/policy/update/company").permitAll()
                        .requestMatchers("/finance/**", "/finance/filter/**").permitAll()

                        .requestMatchers("/subscription/recommendation").authenticated()
                        .requestMatchers("/subscription/city", "/subscription/district", "/subscription/local", "/subscription/update").permitAll()
                        .requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**","/swagger-resources/**","/webjars/**").permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(jwtUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080","https://chapter-1.github.io/" ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
