package com.ecommerce.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldAuthenticateWhenTokenIsValid()
            throws Exception {

        String token = "valid-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractEmail(token))
                .thenReturn("rakshitha@example.com");

        when(jwtService.extractRole(token))
                .thenReturn("USER");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "rakshitha@example.com",
                authentication.getName()
        );

        assertTrue(
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals("ROLE_USER")
                        )
        );

        verify(jwtService)
                .isTokenValid(token);

        verify(jwtService)
                .extractEmail(token);

        verify(jwtService)
                .extractRole(token);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotAuthenticateWhenTokenIsInvalid()
            throws Exception {

        String token = "invalid-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .isTokenValid(token);

        verify(jwtService, never())
                .extractEmail(anyString());

        verify(jwtService, never())
                .extractRole(anyString());

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_shouldClearContextWhenJwtProcessingFails()
            throws Exception {

        String token = "broken-jwt-token";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .isTokenValid(token);

        verify(filterChain)
                .doFilter(request, response);
    }
}