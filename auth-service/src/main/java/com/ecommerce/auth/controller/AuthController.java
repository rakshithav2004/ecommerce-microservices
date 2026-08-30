package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "APIs for user registration, login, and authenticated user information")
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "Register a new user",
      description = "Creates a new user account with the provided registration details.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User registered successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid registration details"),
    @ApiResponse(responseCode = "409", description = "User already exists")
  })
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "400", description = "Invalid login request"),
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
  })
  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @Operation(
      summary = "Get current authenticated user",
      description =
          "Returns information about the currently authenticated user based on the JWT token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Authenticated user information returned"),
    @ApiResponse(responseCode = "401", description = "User is not authenticated")
  })
  @GetMapping("/me")
  public String getCurrentUser(Authentication authentication) {
    return "Authenticated user: " + authentication.getName();
  }
}
