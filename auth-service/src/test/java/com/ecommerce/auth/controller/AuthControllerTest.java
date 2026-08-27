package com.ecommerce.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.security.JwtAuthenticationFilter;
import com.ecommerce.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void register_shouldReturn201() throws Exception {

    RegisterRequest request =
        new RegisterRequest("rakshitha", "rakshitha@example.com", "Password@123", "USER");

    AuthResponse response =
        new AuthResponse("jwt-token", "rakshitha", "rakshitha@example.com", "USER");

    when(authService.register(any(RegisterRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.username").value("rakshitha"))
        .andExpect(jsonPath("$.email").value("rakshitha@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));

    verify(authService).register(any(RegisterRequest.class));
  }

  @Test
  void register_shouldReturn400ForInvalidRequest() throws Exception {

    RegisterRequest invalidRequest = new RegisterRequest("", "", "", "USER");

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).register(any(RegisterRequest.class));
  }

  @Test
  void login_shouldReturn200() throws Exception {

    LoginRequest request = new LoginRequest("rakshitha@example.com", "Password@123");

    AuthResponse response =
        new AuthResponse("jwt-token", "rakshitha", "rakshitha@example.com", "USER");

    when(authService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.username").value("rakshitha"))
        .andExpect(jsonPath("$.email").value("rakshitha@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));

    verify(authService).login(any(LoginRequest.class));
  }

  @Test
  void login_shouldReturn400ForInvalidRequest() throws Exception {

    LoginRequest invalidRequest = new LoginRequest("", "");

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(authService, never()).login(any(LoginRequest.class));
  }

  @Test
  void getCurrentUser_shouldReturn200() throws Exception {

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            "rakshitha@example.com", null, Collections.emptyList());

    mockMvc
        .perform(get("/api/v1/auth/me").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(content().string("Authenticated user: rakshitha@example.com"));
  }
}
