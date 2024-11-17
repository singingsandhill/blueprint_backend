package com.chapter1.blueprint;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI openAPI() {
                Info info = new Info()
                        .title("Project API Documentation")
                        .version("v1.0.0")
                        .description("API 명세서")
                        .contact(new Contact()
                                .name("Chapter 1")
                                .email("example@example.com")
                                .url("https://github.com/Chapter-1"))
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0"));

                // Security 스키마 설정
                SecurityScheme bearerAuth = new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization");

                // Security 요청 설정
                SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

                return new OpenAPI()
                        .openapi("3.0.1")
                        .info(info)
                        .servers(Arrays.asList(
                                new Server().url("http://localhost:8080").description("Local Server"),
                                new Server().url("http://localhost:5173/frontend").description("Production Server")
                        ))
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth", bearerAuth))
                        .addSecurityItem(securityRequirement);
        }
}