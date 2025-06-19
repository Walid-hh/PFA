package com.covoituragedigitalise.trip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@EntityListeners(AuditingEntityListener.class)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false)
    @NotNull(message = "ID du conducteur est obligatoire")
    private Long driverId;

    @Column(name = "departure_location", nullable = false)
    @NotBlank(message = "Lieu de départ est obligatoire")
    private String departureLocation;

    @Column(name = "arrival_location", nullable = false)
    @NotBlank(message = "Lieu d'arrivée est obligatoire")
    private String arrivalLocation;

    @Column(name = "departure_time", nullable = false)
    @NotNull(message = "Heure de départ est obligatoire")
    private LocalDateTime departureTime;

    @Column(name = "available_seats", nullable = false)
    @Positive(message = "Le nombre de places doit être positif")
    private Integer availableSeats;

    @Column(name = "original_seats", nullable = false)
    private Integer originalSeats;

    @Column(name = "price_per_seat", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Prix par place est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal pricePerSeat;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "departure_latitude")
    private Double departureLatitude;

    @Column(name = "departure_longitude")
    private Double departureLongitude;

    @Column(name = "arrival_latitude")
    private Double arrivalLatitude;

    @Column(name = "arrival_longitude")
    private Double arrivalLongitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false)
    private TripStatus tripStatus = TripStatus.ACTIVE;

    @Column(name = "is_instant_booking")
    private Boolean isInstantBooking = false;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Trip() {}

    public Trip(Long driverId, String departureLocation, String arrivalLocation,
                LocalDateTime departureTime, Integer availableSeats, BigDecimal pricePerSeat) {
        this.driverId = driverId;
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.originalSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getDepartureLocation() { return departureLocation; }
    public void setDepartureLocation(String departureLocation) { this.departureLocation = departureLocation; }

    public String getArrivalLocation() { return arrivalLocation; }
    public void setArrivalLocation(String arrivalLocation) { this.arrivalLocation = arrivalLocation; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Integer getOriginalSeats() { return originalSeats; }
    public void setOriginalSeats(Integer originalSeats) { this.originalSeats = originalSeats; }

    public BigDecimal getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(BigDecimal pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getDepartureLatitude() { return departureLatitude; }
    public void setDepartureLatitude(Double departureLatitude) { this.departureLatitude = departureLatitude; }

    public Double getDepartureLongitude() { return departureLongitude; }
    public void setDepartureLongitude(Double departureLongitude) { this.departureLongitude = departureLongitude; }

    public Double getArrivalLatitude() { return arrivalLatitude; }
    public void setArrivalLatitude(Double arrivalLatitude) { this.arrivalLatitude = arrivalLatitude; }

    public Double getArrivalLongitude() { return arrivalLongitude; }
    public void setArrivalLongitude(Double arrivalLongitude) { this.arrivalLongitude = arrivalLongitude; }

    public TripStatus getTripStatus() { return tripStatus; }
    public void setTripStatus(TripStatus tripStatus) { this.tripStatus = tripStatus; }

    public Boolean getIsInstantBooking() { return isInstantBooking; }
    public void setIsInstantBooking(Boolean isInstantBooking) { this.isInstantBooking = isInstantBooking; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Utility methods
    public boolean isActive() {
        return TripStatus.ACTIVE.equals(tripStatus);
    }

    public boolean isFull() {
        return availableSeats <= 0;
    }

    public boolean canBook(int requestedSeats) {
        return isActive() && availableSeats >= requestedSeats;
    }

    public void reserveSeats(int seats) {
        if (canBook(seats)) {
            this.availableSeats -= seats;
        } else {
            throw new IllegalStateException("Impossible de réserver " + seats + " places");
        }
    }

    public void releaseSeats(int seats) {
        this.availableSeats = Math.min(this.availableSeats + seats, this.originalSeats);
    }

    public BigDecimal calculateTotalPrice(int seats) {
        return pricePerSeat.multiply(BigDecimal.valueOf(seats));
    }

    public boolean isDepartureInFuture() {
        return departureTime.isAfter(LocalDateTime.now());
    }
}