package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityConfigTest {

  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private SecretKey secretKey;

  private static final String SECRET = "my-super-secret-key-that-is-at-least-32-characters-long";

  @BeforeEach
  void setUp() {

    // Clear authentication from previous test
    SecurityContextHolder.clearContext();

    JwtService jwtService = new JwtService(SECRET);

    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);

    secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  @AfterEach
  void tearDown() {

    // Make sure authentication does not leak
    // into the next test
    SecurityContextHolder.clearContext();
  }

  private String generateToken(String email, String role) {

    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
        .signWith(secretKey)
        .compact();
  }

  @Test
  void validUserToken_shouldAuthenticateUser() throws ServletException, IOException {

    String token = generateToken("user@example.com", "USER");

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("Authorization", "Bearer " + token);

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);

    assertEquals("user@example.com", authentication.getPrincipal());

    assertTrue(
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void validAdminToken_shouldAuthenticateAdmin() throws ServletException, IOException {

    String token = generateToken("admin@example.com", "ADMIN");

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("Authorization", "Bearer " + token);

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNotNull(authentication);

    assertEquals("admin@example.com", authentication.getPrincipal());

    assertTrue(
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
  }

  @Test
  void missingToken_shouldNotAuthenticate() throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNull(authentication);
  }

  @Test
  void invalidToken_shouldNotAuthenticate() throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("Authorization", "Bearer invalid-token");

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNull(authentication);
  }

  @Test
  void basicAuthenticationHeader_shouldNotAuthenticate() throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("Authorization", "Basic abc123");

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNull(authentication);
  }

  @Test
  void expiredToken_shouldNotAuthenticate() throws ServletException, IOException {

    String token =
        Jwts.builder()
            .subject("expired@example.com")
            .claim("role", "USER")
            .expiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(secretKey)
            .compact();

    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    request.addHeader("Authorization", "Bearer " + token);

    MockFilterChain filterChain = new MockFilterChain();

    jwtAuthenticationFilter.doFilter(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertNull(authentication);
  }
}
