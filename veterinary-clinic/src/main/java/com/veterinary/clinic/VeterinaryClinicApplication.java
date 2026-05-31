package com.veterinary.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VeterinaryClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterinaryClinicApplication.class, args);
    }
}