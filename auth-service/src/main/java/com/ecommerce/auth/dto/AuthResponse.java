package com.ecommerce.auth.dto;

public record AuthResponse(
        String token,
        String username,
        String email,
        String role
) {
}
