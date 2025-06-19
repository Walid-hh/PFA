package com.covoituragedigitalise.trip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Cette classe active l'audit automatique pour @CreatedDate et @LastModifiedDate
}