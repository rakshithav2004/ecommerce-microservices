package com.ecommerce.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
class SecurityConfigTest {

  @Autowired private SecurityFilterChain securityFilterChain;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void securityFilterChainShouldLoad() {
    assertThat(securityFilterChain).isNotNull();
  }

  @Test
  void passwordEncoderShouldBeConfigured() {
    assertThat(passwordEncoder).isNotNull();
    assertThat(passwordEncoder.encode("password")).isNotEqualTo("password");
  }
}
