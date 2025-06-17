package com.covoituragedigitalise.user.service;

import com.covoituragedigitalise.user.dto.UserDto;
import com.covoituragedigitalise.user.dto.UserRegistrationDto;
import com.covoituragedigitalise.user.entity.User;
import com.covoituragedigitalise.user.entity.UserStatus;
import com.covoituragedigitalise.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ MÉTHODE pour créer un utilisateur (utilisée par AuthService)
    public UserDto createUser(UserRegistrationDto registrationDto) {
        try {
            System.out.println("🚀 UserService - Création utilisateur: " + registrationDto.getEmail());

            // Vérifications d'unicité
            if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
            }

            if (registrationDto.getPhone() != null &&
                    userRepository.findByPhone(registrationDto.getPhone()).isPresent()) {
                throw new IllegalArgumentException("Un utilisateur avec ce numéro de téléphone existe déjà");
            }

            // Créer le nouvel utilisateur
            User user = new User();
            user.setEmail(registrationDto.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            user.setPhone(registrationDto.getPhone());
            user.setStatus(UserStatus.ACTIVE);
            user.setIsVerified(false);
            user.setIsDriver(false);
            user.setTotalTrips(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Sauvegarder
            User savedUser = userRepository.save(user);
            System.out.println("✅ UserService - Utilisateur créé avec ID: " + savedUser.getId());

            return convertToUserDto(savedUser);

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur createUser: " + e.getMessage());
            throw e;
        }
    }

    // ✅ MÉTHODE pour trouver un utilisateur par email
    public User findUserByEmail(String email) {
        try {
            System.out.println("🔍 UserService - Recherche utilisateur: " + email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));

            System.out.println("✅ UserService - Utilisateur trouvé: " + user.getId());
            return user;

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur findUserByEmail: " + e.getMessage());
            throw e;
        }
    }

    // ✅ MÉTHODE pour récupérer UserDto par email (utilisée par AuthService)
    public UserDto getUserByEmail(String email) {
        try {
            User user = findUserByEmail(email);
            return convertToUserDto(user);
        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur getUserByEmail: " + e.getMessage());
            throw e;
        }
    }

    // ✅ MÉTHODE pour vérifier les credentials lors du login
    public boolean validateCredentials(String email, String rawPassword) {
        try {
            System.out.println("🔐 UserService - Validation credentials: " + email);

            User user = findUserByEmail(email);

            if (!user.isActive()) {
                throw new IllegalArgumentException("Compte désactivé");
            }

            boolean isValid = passwordEncoder.matches(rawPassword, user.getPassword());
            System.out.println("✅ UserService - Credentials valides: " + isValid);

            return isValid;

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur validateCredentials: " + e.getMessage());
            throw e;
        }
    }

    // ✅ MÉTHODE de conversion User -> UserDto
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        return dto;
    }

    // ✅ NOUVELLE MÉTHODE pour mettre à jour le profil
    public User updateUserProfile(String email, Map<String, Object> updateData) {
        try {
            System.out.println("🔄 UserService - Mise à jour profil: " + email);

            User user = findUserByEmail(email);

            // Mettre à jour les champs autorisés
            if (updateData.containsKey("firstName")) {
                user.setFirstName((String) updateData.get("firstName"));
            }
            if (updateData.containsKey("lastName")) {
                user.setLastName((String) updateData.get("lastName"));
            }
            if (updateData.containsKey("phone")) {
                String newPhone = (String) updateData.get("phone");
                // Vérifier si le téléphone est déjà utilisé par un autre utilisateur
                if (newPhone != null && !newPhone.equals(user.getPhone())) {
                    if (userRepository.findByPhone(newPhone).isPresent()) {
                        throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
                    }
                }
                user.setPhone(newPhone);
            }
            if (updateData.containsKey("bio")) {
                user.setBio((String) updateData.get("bio"));
            }
            if (updateData.containsKey("dateOfBirth")) {
                // Gérer la date de naissance si fournie
                String dateStr = (String) updateData.get("dateOfBirth");
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        user.setDateOfBirth(java.time.LocalDate.parse(dateStr));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Format de date invalide. Utilisez YYYY-MM-DD");
                    }
                }
            }

            user.setUpdatedAt(java.time.LocalDateTime.now());

            User savedUser = userRepository.save(user);
            System.out.println("✅ UserService - Profil mis à jour: " + savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur updateUserProfile: " + e.getMessage());
            throw e;
        }
    }

    // ✅ NOUVELLE MÉTHODE pour changer le mot de passe
    public void changePassword(String email, String currentPassword, String newPassword) {
        try {
            System.out.println("🔐 UserService - Changement mot de passe: " + email);

            User user = findUserByEmail(email);

            // Vérifier le mot de passe actuel
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Mot de passe actuel incorrect");
            }

            // Valider le nouveau mot de passe
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères");
            }

            // Mettre à jour le mot de passe
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(java.time.LocalDateTime.now());

            userRepository.save(user);
            System.out.println("✅ UserService - Mot de passe changé: " + user.getId());

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur changePassword: " + e.getMessage());
            throw e;
        }
    }

    // ✅ NOUVELLE MÉTHODE pour devenir conducteur
    public User becomeDriver(String email, String driverLicense) {
        try {
            System.out.println("🚗 UserService - Devenir conducteur: " + email);

            User user = findUserByEmail(email);

            // Vérifier si déjà conducteur
            if (user.getIsDriver()) {
                throw new IllegalArgumentException("Vous êtes déjà conducteur");
            }

            // Valider le permis de conduire
            if (driverLicense == null || driverLicense.trim().isEmpty()) {
                throw new IllegalArgumentException("Numéro de permis de conduire requis");
            }

            // Vérifier si le permis n'est pas déjà utilisé
            if (userRepository.findByDriverLicense(driverLicense).isPresent()) {
                throw new IllegalArgumentException("Ce numéro de permis de conduire est déjà utilisé");
            }

            // Mettre à jour l'utilisateur
            user.setIsDriver(true);
            user.setDriverLicense(driverLicense);
            user.setUpdatedAt(java.time.LocalDateTime.now());

            User savedUser = userRepository.save(user);
            System.out.println("✅ UserService - Conducteur créé: " + savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur becomeDriver: " + e.getMessage());
            throw e;
        }
    }

    // ✅ NOUVELLE MÉTHODE pour désactiver le compte
    public void deactivateAccount(String email) {
        try {
            System.out.println("🗑️ UserService - Désactivation compte: " + email);

            User user = findUserByEmail(email);

            // Marquer comme inactif
            user.setStatus(com.covoituragedigitalise.user.entity.UserStatus.INACTIVE);
            user.setUpdatedAt(java.time.LocalDateTime.now());

            userRepository.save(user);
            System.out.println("✅ UserService - Compte désactivé: " + user.getId());

        } catch (Exception e) {
            System.err.println("❌ UserService - Erreur deactivateAccount: " + e.getMessage());
            throw e;
        }
    }
}