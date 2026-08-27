package com.ecommerce.auth.security;

import com.ecommerce.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expiration;

  public JwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    this.expiration = expiration;
  }

  public String generateToken(User user) {

    Date now = new Date();

    return Jwts.builder()
        .subject(user.getEmail())
        .claim("username", user.getUsername())
        .claim("role", user.getRole())
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expiration))
        .signWith(secretKey)
        .compact();
  }

  public String extractEmail(String token) {

    return getClaims(token).getSubject();
  }

  public String extractUsername(String token) {

    return getClaims(token).get("username", String.class);
  }

  public String extractRole(String token) {

    return getClaims(token).get("role", String.class);
  }

  public boolean isTokenValid(String token) {

    try {
      getClaims(token);
      return true;
    } catch (Exception exception) {
      return false;
    }
  }

  private Claims getClaims(String token) {

    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
