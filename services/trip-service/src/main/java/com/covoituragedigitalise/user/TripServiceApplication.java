package com.covoituragedigitalise.trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TripServiceApplication {

    public static void main(String[] args) {
        System.out.println("🚗 Démarrage du Trip Service...");
        SpringApplication.run(TripServiceApplication.class, args);
        System.out.println("✅ Trip Service démarré avec succès!");
    }
}