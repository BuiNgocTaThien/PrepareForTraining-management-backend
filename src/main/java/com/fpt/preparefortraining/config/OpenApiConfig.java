package com.fpt.preparefortraining.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI knowledgeBaseOpenApi() {
    return new OpenAPI().info(new Info().title("PrepareForTraining Management API").version("v1"));
  }
}
