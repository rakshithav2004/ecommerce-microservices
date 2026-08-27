package com.ecommerce.gateway.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private JwtService jwtService;

  private static final String SECRET = "my-super-secret-key-that-is-at-least-32-characters-long";

  private SecretKey secretKey;

  @BeforeEach
  void setUp() {

    jwtService = new JwtService(SECRET);

    secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void isTokenValid_shouldReturnTrueForValidToken() {

    String token =
        Jwts.builder()
            .subject("test@example.com")
            .claim("role", "USER")
            .signWith(secretKey)
            .compact();

    boolean result = jwtService.isTokenValid(token);

    assertTrue(result);
  }

  @Test
  void isTokenValid_shouldReturnFalseForInvalidToken() {

    String token = "invalid.jwt.token";

    boolean result = jwtService.isTokenValid(token);

    assertFalse(result);
  }

  @Test
  void isTokenValid_shouldReturnFalseForTamperedToken() {

    String token =
        Jwts.builder()
            .subject("test@example.com")
            .claim("role", "USER")
            .signWith(secretKey)
            .compact();

    String tamperedToken = token.substring(0, token.length() - 2) + "xx";

    boolean result = jwtService.isTokenValid(tamperedToken);

    assertFalse(result);
  }

  @Test
  void isTokenValid_shouldReturnFalseForExpiredToken() {

    Date expiration = new Date(System.currentTimeMillis() - 1000);

    String token =
        Jwts.builder()
            .subject("test@example.com")
            .claim("role", "USER")
            .expiration(expiration)
            .signWith(secretKey)
            .compact();

    boolean result = jwtService.isTokenValid(token);

    assertFalse(result);
  }

  @Test
  void extractEmail_shouldReturnSubject() {

    String token =
        Jwts.builder()
            .subject("test@example.com")
            .claim("role", "USER")
            .signWith(secretKey)
            .compact();

    String email = jwtService.extractEmail(token);

    assertEquals("test@example.com", email);
  }

  @Test
  void extractRole_shouldReturnRole() {

    String token =
        Jwts.builder()
            .subject("test@example.com")
            .claim("role", "USER")
            .signWith(secretKey)
            .compact();

    String role = jwtService.extractRole(token);

    assertEquals("USER", role);
  }

  @Test
  void extractRole_shouldReturnAdminRole() {

    String token =
        Jwts.builder()
            .subject("admin@example.com")
            .claim("role", "ADMIN")
            .signWith(secretKey)
            .compact();

    String role = jwtService.extractRole(token);

    assertEquals("ADMIN", role);
  }
}
