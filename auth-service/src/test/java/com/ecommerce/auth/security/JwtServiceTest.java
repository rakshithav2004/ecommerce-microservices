package com.ecommerce.auth.security;

import com.ecommerce.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret =
            "my-super-secret-key-for-jwt-authentication-123456789";
    private final long expiration =
            3600000L;
    private User user;


    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                secret,
                expiration
        );

        user = User.builder()
                .username("rakshitha")
                .email("rakshitha@example.com")
                .password("encoded-password")
                .role("USER")
                .build();
    }

    @Test
    void generateToken_shouldGenerateValidToken() {

        String token =
                jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertTrue(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void extractEmail_shouldReturnUserEmail() {

        String token =
                jwtService.generateToken(user);

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "rakshitha@example.com",
                email
        );
    }

    @Test
    void extractUsername_shouldReturnUsername() {

        String token =
                jwtService.generateToken(user);

        String username =
                jwtService.extractUsername(token);

        assertEquals(
                "rakshitha",
                username
        );
    }

    @Test
    void extractRole_shouldReturnUserRole() {

        String token =
                jwtService.generateToken(user);

        String role =
                jwtService.extractRole(token);

        assertEquals(
                "USER",
                role
        );
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token =
                jwtService.generateToken(user);

        boolean result =
                jwtService.isTokenValid(token);

        assertTrue(result);
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {

        String invalidToken =
                "invalid.jwt.token";

        boolean result =
                jwtService.isTokenValid(invalidToken);

        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalseForTamperedToken() {

        String token =
                jwtService.generateToken(user);

        String tamperedToken =
                token.substring(0, token.length() - 2)
                        + "ab";

        boolean result =
                jwtService.isTokenValid(tamperedToken);

        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {

        JwtService expiredJwtService =
                new JwtService(
                        secret,
                        -1000L
                );

        String token =
                expiredJwtService.generateToken(user);

        boolean result =
                expiredJwtService.isTokenValid(token);

        assertFalse(result);
    }
}