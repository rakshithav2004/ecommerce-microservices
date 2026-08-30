package com.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-key-for-testing-only")
class ApiGatewayApplicationTests {

  @Test
  void contextLoads() {}
}
