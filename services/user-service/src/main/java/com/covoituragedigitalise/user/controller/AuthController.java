package com.covoituragedigitalise.user.controller;

import com.covoituragedigitalise.user.dto.UserDto;
import com.covoituragedigitalise.user.dto.UserRegistrationDto;
import com.covoituragedigitalise.user.service.UserService;
import com.covoituragedigitalise.user.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        try {
            System.out.println("🚀 AUTH - INSCRIPTION - Email: " + registrationDto.getEmail());

            UserDto userDto = userService.createUser(registrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 201);
            response.put("message", "Utilisateur créé avec succès");
            response.put("user", Map.of(
                    "email", userDto.getEmail(),
                    "firstName", userDto.getFirstName(),
                    "lastName", userDto.getLastName(),
                    "phone", userDto.getPhone() != null ? userDto.getPhone() : ""
            ));

            System.out.println("🎉 AUTH - INSCRIPTION - Terminée avec succès!");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ AUTH - INSCRIPTION - Erreur: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 400);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            System.err.println("💥 AUTH - INSCRIPTION - ERREUR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Une erreur interne s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDto loginDto) {
        try {
            System.out.println("🔐 AUTH - LOGIN - Email: " + loginDto.getEmail());

            // Valider les credentials
            boolean isValid = userService.validateCredentials(loginDto.getEmail(), loginDto.getPassword());

            if (!isValid) {
                System.out.println("❌ AUTH - LOGIN - Credentials invalides");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "Email ou mot de passe incorrect"));
            }

            // Récupérer l'utilisateur
            UserDto user = userService.getUserByEmail(loginDto.getEmail());

            // Générer le token JWT
            String token = jwtService.generateToken(loginDto.getEmail());

            // Réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Connexion réussie");
            response.put("token", token);
            response.put("user", Map.of(
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "phone", user.getPhone() != null ? user.getPhone() : ""
            ));

            System.out.println("✅ AUTH - LOGIN - Connexion réussie pour: " + user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ AUTH - LOGIN - ERREUR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Email ou mot de passe incorrect"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> authHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Auth Service is running");
        response.put("timestamp", System.currentTimeMillis());

        System.out.println("💚 AUTH - HEALTH CHECK - Service OK");
        return ResponseEntity.ok(response);
    }
}