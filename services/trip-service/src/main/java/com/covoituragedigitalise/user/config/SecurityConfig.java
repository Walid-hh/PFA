package com.covoituragedigitalise.trip.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 SecurityConfig - Configuration avec JwtAuthenticationFilter");

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Endpoints publics
                        .requestMatchers("/api/*/health").permitAll()
                        .requestMatchers("/api/trips/search").permitAll()
                        .requestMatchers("/api/trips/{tripId}").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Endpoints protégés - Trajets
                        .requestMatchers("/api/trips").authenticated()
                        .requestMatchers("/api/trips/my-trips").authenticated()
                        .requestMatchers("/api/trips/stats").authenticated()

                        // Endpoints protégés - Réservations
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}