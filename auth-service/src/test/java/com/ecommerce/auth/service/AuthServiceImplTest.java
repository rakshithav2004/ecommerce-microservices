package com.ecommerce.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtService jwtService;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  void register_shouldCreateUserSuccessfully() {

    RegisterRequest request =
        new RegisterRequest("rakshitha", "rakshitha@example.com", "Password@123", "USER");

    User savedUser =
        User.builder()
            .username("rakshitha")
            .email("rakshitha@example.com")
            .password("encoded-password")
            .role("USER")
            .build();

    when(userRepository.existsByEmail("rakshitha@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals("jwt-token", response.token());
    assertEquals("rakshitha", response.username());
    assertEquals("rakshitha@example.com", response.email());
    assertEquals("USER", response.role());

    verify(userRepository).existsByEmail("rakshitha@example.com");
    verify(passwordEncoder).encode("Password@123");
    verify(userRepository).save(any(User.class));
    verify(jwtService).generateToken(savedUser);
  }

  @Test
  void register_shouldDefaultRoleToUserWhenRoleIsBlank() {

    RegisterRequest request =
        new RegisterRequest("rakshitha", "rakshitha@example.com", "Password@123", "");

    when(userRepository.existsByEmail("rakshitha@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");

    User savedUser =
        User.builder()
            .username("rakshitha")
            .email("rakshitha@example.com")
            .password("encoded-password")
            .role("USER")
            .build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals("USER", response.role());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User user = userCaptor.getValue();

    assertEquals("rakshitha", user.getUsername());
    assertEquals("rakshitha@example.com", user.getEmail());
    assertEquals("encoded-password", user.getPassword());
    assertEquals("USER", user.getRole());

    verify(passwordEncoder).encode("Password@123");
    verify(jwtService).generateToken(savedUser);
  }

  @Test
  void register_shouldAllowAdminRole() {

    RegisterRequest request =
        new RegisterRequest("admin", "admin@example.com", "Password@123", "admin");

    when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");

    User savedUser =
        User.builder()
            .username("admin")
            .email("admin@example.com")
            .password("encoded-password")
            .role("ADMIN")
            .build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateToken(savedUser)).thenReturn("admin-jwt-token");

    AuthResponse response = authService.register(request);

    assertEquals("ADMIN", response.role());
    assertEquals("admin-jwt-token", response.token());
  }

  @Test
  void register_shouldThrowExceptionWhenEmailAlreadyExists() {

    RegisterRequest request =
        new RegisterRequest("rakshitha", "rakshitha@example.com", "Password@123", "USER");

    when(userRepository.existsByEmail("rakshitha@example.com")).thenReturn(true);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> authService.register(request));

    assertEquals(
        "User with email already exists: " + "rakshitha@example.com", exception.getMessage());

    verify(userRepository).existsByEmail("rakshitha@example.com");
    verify(userRepository, never()).save(any(User.class));
    verify(passwordEncoder, never()).encode(anyString());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void register_shouldThrowExceptionForInvalidRole() {

    RegisterRequest request =
        new RegisterRequest("rakshitha", "rakshitha@example.com", "Password@123", "MANAGER");

    when(userRepository.existsByEmail("rakshitha@example.com")).thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

    assertEquals("Role must be USER or ADMIN", exception.getMessage());

    verify(userRepository).existsByEmail("rakshitha@example.com");
    verify(userRepository, never()).save(any(User.class));
    verify(passwordEncoder, never()).encode(anyString());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_shouldReturnAuthResponse() {

    LoginRequest request = new LoginRequest("rakshitha@example.com", "Password@123");

    User user =
        User.builder()
            .username("rakshitha")
            .email("rakshitha@example.com")
            .password("encoded-password")
            .role("USER")
            .build();

    when(userRepository.findByEmail("rakshitha@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    AuthResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("jwt-token", response.token());
    assertEquals("rakshitha", response.username());
    assertEquals("rakshitha@example.com", response.email());
    assertEquals("USER", response.role());

    verify(userRepository).findByEmail("rakshitha@example.com");
    verify(passwordEncoder).matches("Password@123", "encoded-password");
    verify(jwtService).generateToken(user);
  }

  @Test
  void login_shouldThrowExceptionWhenUserNotFound() {

    LoginRequest request = new LoginRequest("unknown@example.com", "Password@123");

    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> authService.login(request));

    assertEquals("Invalid email or password", exception.getMessage());

    verify(userRepository).findByEmail("unknown@example.com");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void login_shouldThrowExceptionForWrongPassword() {

    LoginRequest request = new LoginRequest("rakshitha@example.com", "WrongPassword");

    User user =
        User.builder()
            .username("rakshitha")
            .email("rakshitha@example.com")
            .password("encoded-password")
            .role("USER")
            .build();

    when(userRepository.findByEmail("rakshitha@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPassword", "encoded-password")).thenReturn(false);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> authService.login(request));

    assertEquals("Invalid email or password", exception.getMessage());

    verify(passwordEncoder).matches("WrongPassword", "encoded-password");
    verify(jwtService, never()).generateToken(any());
  }
}
