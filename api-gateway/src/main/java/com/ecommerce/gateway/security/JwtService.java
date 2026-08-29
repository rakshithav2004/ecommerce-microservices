package com.ecommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;

  public JwtService(@Value("${jwt.secret}") String secret) {

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public boolean isTokenValid(String token) {

    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

      return true;

    } catch (Exception exception) {

      return false;
    }
  }

  public String extractEmail(String token) {

    return getClaims(token).getSubject();
  }

  public String extractRole(String token) {

    return getClaims(token).get("role", String.class);
  }

  public String extractCustomerId(String token) {

    return getClaims(token).get("customerId", String.class);
  }

  private Claims getClaims(String token) {

    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
