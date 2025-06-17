package com.covoituragedigitalise.user.service;

import com.covoituragedigitalise.user.dto.UserLoginDto;
import com.covoituragedigitalise.user.dto.UserResponseDto;
import com.covoituragedigitalise.user.dto.UserRegistrationDto;
import com.covoituragedigitalise.user.dto.UserDto;
import com.covoituragedigitalise.user.entity.User;
import com.covoituragedigitalise.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    // ✅ MÉTHODE d'inscription
    public UserDto register(UserRegistrationDto request) {
        try {
            System.out.println("🚀 AuthService - Inscription: " + request.getEmail());
            return userService.createUser(request);
        } catch (Exception e) {
            System.err.println("❌ AuthService - Erreur inscription: " + e.getMessage());
            throw e;
        }
    }

    // ✅ MÉTHODE d'authentification
    public UserResponseDto authenticate(UserLoginDto request) {
        try {
            System.out.println("🔐 AuthService - Authentification: " + request.getEmail());

            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Récupérer l'utilisateur
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Générer le token JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String jwtToken = jwtService.generateToken(userDetails);

            // Convertir en DTO
            UserDto userDto = userService.getUserByEmail(request.getEmail());

            // Retourner la réponse avec token
            UserResponseDto response = new UserResponseDto(jwtToken, userDto);
            System.out.println("✅ AuthService - Authentification réussie");

            return response;

        } catch (Exception e) {
            System.err.println("❌ AuthService - Erreur authentification: " + e.getMessage());
            throw e;
        }
    }
}