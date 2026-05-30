package com.veterinary.clinic;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI veterinaryClinicOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("System Zarządzania Kliniką Weterynaryjną")
                        .description("""
                                REST API do zarządzania kliniką weterynaryjną.
                                
                                Funkcjonalności:
                                - Zarządzanie lekarzami (CRUD)
                                - Zarządzanie pacjentami/zwierzętami (CRUD)
                                - Rejestracja wizyt z walidacją konfliktów czasowych (okno 30 minutowe)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("z Kliniką : kontakt@klinika-wet.pl")
                                .email("kontakt@klinika-wet.pl")));
    }
}