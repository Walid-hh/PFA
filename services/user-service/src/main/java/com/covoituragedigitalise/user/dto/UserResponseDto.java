package com.covoituragedigitalise.user.dto;

import com.covoituragedigitalise.user.entity.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private Boolean isVerified;
    private Boolean isDriver;
    private BigDecimal rating;
    private Integer totalTrips;
    private String profilePictureUrl;
    private String bio;
    private LocalDateTime dateOfBirth;
    private LocalDateTime createdAt;

    // ✅ NOUVEAU: Token pour AuthService
    private String token;

    // ✅ Constructeur par défaut
    public UserResponseDto() {}

    // ✅ NOUVEAU: Constructeur pour AuthService (token + userDto)
    public UserResponseDto(String token, UserDto userDto) {
        this.token = token;
        this.email = userDto.getEmail();
        this.firstName = userDto.getFirstName();
        this.lastName = userDto.getLastName();
        this.phone = userDto.getPhone();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsDriver() { return isDriver; }
    public void setIsDriver(Boolean isDriver) { this.isDriver = isDriver; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getTotalTrips() { return totalTrips; }
    public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ✅ NOUVEAU: Getter/Setter pour token
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}