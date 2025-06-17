package com.covoituragedigitalise.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du User Service...");
        SpringApplication.run(UserServiceApplication.class, args);
        System.out.println("✅ User Service démarré avec succès!");
    }
}