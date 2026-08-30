package com.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-key-for-testing-only")
class AuthServiceApplicationTests {

  @Test
  void contextLoads() {}
}
