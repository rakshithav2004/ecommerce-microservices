package com.ecommerce.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI authServiceOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("E-Commerce Auth Service API")
                .description(
                    "REST APIs for user registration, authentication, "
                        + "JWT token generation, and user management.")
                .version("1.0.0"));
  }
}
