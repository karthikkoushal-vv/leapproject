package com.foodshare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI foodShareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🍲 FoodShare API Documentation")
                        .description("Surplus Food Donation Matching & Automated Waste Diversion Platform REST APIs.\n\n" +
                                "### Core Capabilities:\n" +
                                "- **Donors:** Register campus canteens & caterers\n" +
                                "- **Listings:** Create, browse, update surplus food with expiry window\n" +
                                "- **NGOs:** Register orphanages & shelters\n" +
                                "- **Claims:** Claim available food and mark collected\n" +
                                "- **Analytics:** Monthly waste diversion statistics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FoodShare Support")
                                .email("support@foodshare.org")));
    }
}
