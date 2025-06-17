package com.covoituragedigitalise.user.service;

import com.covoituragedigitalise.user.entity.User;
import com.covoituragedigitalise.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            System.out.println("🔍 UserDetailsService - Recherche utilisateur: " + email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));

            System.out.println("✅ UserDetailsService - Utilisateur trouvé: " + user.getEmail());

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(new ArrayList<>()) // Pas de rôles pour l'instant
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!user.isActive())
                    .build();

        } catch (Exception e) {
            System.err.println("❌ UserDetailsService - Erreur: " + e.getMessage());
            throw new UsernameNotFoundException("Erreur lors de la recherche de l'utilisateur: " + email, e);
        }
    }
}