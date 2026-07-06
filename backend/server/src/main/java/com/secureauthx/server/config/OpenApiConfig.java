package com.secureauthx.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI secureAuthXOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecureAuthX API")
                        .description("SecureAuthX authentication and identity platform API.")
                        .version("0.0.1")
                        .contact(new Contact().name("SecureAuthX Engineering"))
                        .license(new License().name("See repository LICENSE")));
    }
}
