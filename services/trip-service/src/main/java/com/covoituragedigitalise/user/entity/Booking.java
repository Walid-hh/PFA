package com.covoituragedigitalise.trip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @NotNull(message = "Trajet est obligatoire")
    private Trip trip;

    @Column(name = "passenger_id", nullable = false)
    @NotNull(message = "ID du passager est obligatoire")
    private Long passengerId;

    @Column(name = "seats_booked", nullable = false)
    @Positive(message = "Le nombre de places doit être positif")
    private Integer seatsBooked;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Prix total est obligatoire")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "dropoff_location")
    private String dropoffLocation;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "passenger_phone")
    private String passengerPhone;

    @Column(name = "passenger_name")
    private String passengerName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Booking() {
        this.bookingDate = LocalDateTime.now();
    }

    public Booking(Trip trip, Long passengerId, Integer seatsBooked, BigDecimal totalPrice) {
        this.trip = trip;
        this.passengerId = passengerId;
        this.seatsBooked = seatsBooked;
        this.totalPrice = totalPrice;
        this.bookingDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPassengerPhone() { return passengerPhone; }
    public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Utility methods
    public boolean isPending() {
        return BookingStatus.PENDING.equals(bookingStatus);
    }

    public boolean isConfirmed() {
        return BookingStatus.CONFIRMED.equals(bookingStatus);
    }

    public boolean isCancelled() {
        return BookingStatus.CANCELLED.equals(bookingStatus);
    }

    public boolean isRejected() {
        return BookingStatus.REJECTED.equals(bookingStatus);
    }

    public boolean canBeCancelled() {
        return isPending() || isConfirmed();
    }

    public boolean canBeConfirmed() {
        return isPending();
    }

    public void confirm() {
        if (canBeConfirmed()) {
            this.bookingStatus = BookingStatus.CONFIRMED;
        } else {
            throw new IllegalStateException("Impossible de confirmer cette réservation");
        }
    }

    public void cancel() {
        if (canBeCancelled()) {
            this.bookingStatus = BookingStatus.CANCELLED;
        } else {
            throw new IllegalStateException("Impossible d'annuler cette réservation");
        }
    }

    public void reject() {
        if (isPending()) {
            this.bookingStatus = BookingStatus.REJECTED;
        } else {
            throw new IllegalStateException("Impossible de rejeter cette réservation");
        }
    }
}