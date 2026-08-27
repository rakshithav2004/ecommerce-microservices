package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUp() {
    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);

    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilter_shouldContinueWhenAuthorizationHeaderIsMissing()
      throws ServletException, IOException {

    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    verifyNoInteractions(jwtService);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilter_shouldContinueWhenAuthorizationHeaderIsNotBearer()
      throws ServletException, IOException {

    when(request.getHeader("Authorization")).thenReturn("Basic abc123");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);

    verifyNoInteractions(jwtService);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilter_shouldAuthenticateWhenTokenIsValid() throws ServletException, IOException {

    String token = "valid-token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

    when(jwtService.isTokenValid(token)).thenReturn(true);

    when(jwtService.extractEmail(token)).thenReturn("test@example.com");

    when(jwtService.extractRole(token)).thenReturn("USER");

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);

    assertEquals("test@example.com", authentication.getPrincipal());

    assertTrue(
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));

    verify(jwtService).isTokenValid(token);
    verify(jwtService).extractEmail(token);
    verify(jwtService).extractRole(token);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilter_shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {

    String token = "invalid-token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

    when(jwtService.isTokenValid(token)).thenReturn(false);

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());

    verify(jwtService).isTokenValid(token);

    verify(jwtService, never()).extractEmail(token);

    verify(jwtService, never()).extractRole(token);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilter_shouldClearContextWhenJwtServiceThrowsException()
      throws ServletException, IOException {

    String token = "invalid-token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

    when(jwtService.isTokenValid(token)).thenThrow(new RuntimeException("Invalid JWT"));

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());

    verify(jwtService).isTokenValid(token);

    verify(filterChain).doFilter(request, response);
  }
}
