package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public AuthResponse register(RegisterRequest request) {

    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("User with email already exists: " + request.email());
    }

    String role = request.role();

    if (role == null || role.isBlank()) {
      role = "USER";
    }

    role = role.toUpperCase();

    if (!role.equals("USER") && !role.equals("ADMIN")) {
      throw new IllegalArgumentException("Role must be USER or ADMIN");
    }

    User user =
        User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(role)
            .build();

    User savedUser = userRepository.save(user);

    String token = jwtService.generateToken(savedUser);

    return new AuthResponse(
        token, savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
  }

  @Override
  public AuthResponse login(LoginRequest request) {

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new IllegalStateException("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new IllegalStateException("Invalid email or password");
    }

    String token = jwtService.generateToken(user);

    return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
  }
}
