package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.security.util.JwtProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private JwtProcessor jwtProcessor;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAuthenticatedUid_Success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        String token = "mockToken";
        when(securityContext.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken(null, token));

        Long mockUid = 123L;
        when(jwtProcessor.getUid(token)).thenReturn(mockUid);

        Long uid = memberService.getAuthenticatedUid();

        assertNotNull(uid);
        assertEquals(mockUid, uid);
        verify(jwtProcessor, times(1)).getUid(token);
    }

    @Test
    void testGetAuthenticatedUid_NoAuthentication() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.getAuthenticatedUid();
        });
        assertEquals("No authentication found", exception.getMessage());
    }

    @Test
    void testGetAuthenticatedUid_InvalidCredentials() {

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken(null, 123)); // Invalid credentials


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.getAuthenticatedUid();
        });
        assertEquals("Invalid authentication credentials", exception.getMessage());
    }
}
