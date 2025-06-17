package com.covoituragedigitalise.user.controller;

import com.covoituragedigitalise.user.entity.User;
import com.covoituragedigitalise.user.service.JwtService;
import com.covoituragedigitalise.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "User Service is running");
        response.put("timestamp", System.currentTimeMillis());

        System.out.println("💚 USER - HEALTH CHECK - Service OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-profile")
    public ResponseEntity<?> testProfile(@RequestParam String email) {
        try {
            System.out.println("🧪 USER - TEST PROFILE - Email: " + email);

            User user = userService.findUserByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Test profile sans auth");
            response.put("user", Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "phone", user.getPhone(),
                    "isVerified", user.getIsVerified(),
                    "totalTrips", user.getTotalTrips()
            ));

            System.out.println("✅ USER - TEST PROFILE - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ USER - TEST PROFILE - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", "Utilisateur non trouvé: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🔍 USER - PROFILE - Début de la requête");
            System.out.println("🔍 USER - PROFILE - Authorization header: " +
                    (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 20)) + "..." : "null"));

            // Extraire le token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ USER - PROFILE - Token manquant ou malformé");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "Token manquant ou malformé"));
            }

            String token = authHeader.substring(7);
            System.out.println("🔑 USER - PROFILE - Token extrait (20 premiers chars): " +
                    token.substring(0, Math.min(token.length(), 20)) + "...");

            // Extraire l'email du token
            String email = jwtService.extractUsername(token);
            System.out.println("📧 USER - PROFILE - Email extrait du token: " + email);

            // Récupérer l'utilisateur
            User user = userService.findUserByEmail(email);
            System.out.println("👤 USER - PROFILE - Utilisateur trouvé: " + user.getEmail());

            // Construire la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Profil récupéré avec succès");
            response.put("user", Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "phone", user.getPhone() != null ? user.getPhone() : "",
                    "isVerified", user.getIsVerified(),
                    "isDriver", user.getIsDriver(),
                    "totalTrips", user.getTotalTrips(),
                    "status", user.getStatus().toString(),
                    "createdAt", user.getCreatedAt()
            ));

            System.out.println("✅ USER - PROFILE - Profil récupéré avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ USER - PROFILE - Erreur: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", 401,
                            "message", "Erreur lors de la récupération du profil",
                            "error", e.getMessage()
                    ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);
            User user = userService.findUserByEmail(email);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTrips", user.getTotalTrips());
            stats.put("isDriver", user.getIsDriver());
            stats.put("isVerified", user.getIsVerified());
            stats.put("memberSince", user.getCreatedAt());
            stats.put("rating", user.getRating() != null ? user.getRating() : 0);

            return ResponseEntity.ok(Map.of("status", 200, "stats", stats));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updateData) {
        try {
            System.out.println("🔄 USER - UPDATE PROFILE - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            User updatedUser = userService.updateUserProfile(email, updateData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Profil mis à jour avec succès");
            response.put("user", Map.of(
                    "userId", updatedUser.getId(),
                    "email", updatedUser.getEmail(),
                    "firstName", updatedUser.getFirstName(),
                    "lastName", updatedUser.getLastName(),
                    "phone", updatedUser.getPhone() != null ? updatedUser.getPhone() : "",
                    "bio", updatedUser.getBio() != null ? updatedUser.getBio() : "",
                    "dateOfBirth", updatedUser.getDateOfBirth(),
                    "isVerified", updatedUser.getIsVerified(),
                    "isDriver", updatedUser.getIsDriver()
            ));

            System.out.println("✅ USER - UPDATE PROFILE - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ USER - UPDATE PROFILE - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> passwordData) {
        try {
            System.out.println("🔐 USER - CHANGE PASSWORD - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", 400, "message", "Current password et new password requis"));
            }

            userService.changePassword(email, currentPassword, newPassword);

            System.out.println("✅ USER - CHANGE PASSWORD - Succès");
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Mot de passe modifié avec succès"
            ));

        } catch (Exception e) {
            System.err.println("❌ USER - CHANGE PASSWORD - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    @PostMapping("/become-driver")
    public ResponseEntity<?> becomeDriver(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> driverData) {
        try {
            System.out.println("🚗 USER - BECOME DRIVER - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            String driverLicense = driverData.get("driverLicense");
            if (driverLicense == null || driverLicense.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", 400, "message", "Numéro de permis de conduire requis"));
            }

            User user = userService.becomeDriver(email, driverLicense);

            System.out.println("✅ USER - BECOME DRIVER - Succès");
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Vous êtes maintenant conducteur !",
                    "user", Map.of(
                            "userId", user.getId(),
                            "isDriver", user.getIsDriver(),
                            "driverLicense", user.getDriverLicense()
                    )
            ));

        } catch (Exception e) {
            System.err.println("❌ USER - BECOME DRIVER - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🗑️ USER - DELETE ACCOUNT - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            userService.deactivateAccount(email);

            System.out.println("✅ USER - DELETE ACCOUNT - Succès");
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Compte désactivé avec succès"
            ));

        } catch (Exception e) {
            System.err.println("❌ USER - DELETE ACCOUNT - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }
}