package com.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI inventoryAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Management API")
                        .version("1.0")
                        .description("Inventory Management System using Spring Boot")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your@email.com")));
    }
}