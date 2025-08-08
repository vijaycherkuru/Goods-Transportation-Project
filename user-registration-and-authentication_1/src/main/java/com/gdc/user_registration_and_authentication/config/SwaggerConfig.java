package com.gdc.user_registration_and_authentication.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Peer-to-Peer Goods Transportation System API")
                        .version("1.0.0")
                        .description("API documentation for User Registration and Authentication module."));
    }
}

