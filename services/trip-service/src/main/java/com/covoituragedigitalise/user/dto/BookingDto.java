package com.covoituragedigitalise.trip.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BookingDto {

    @NotNull(message = "ID du trajet obligatoire")
    private Long tripId;

    @NotNull(message = "Nombre de places obligatoire")
    @Positive(message = "Le nombre de places doit être positif")
    private Integer seatsBooked;

    private String pickupLocation;
    private String dropoffLocation;
    private String specialRequests;
    private String passengerPhone;
    private String passengerName;

    // Constructors
    public BookingDto() {}

    public BookingDto(Long tripId, Integer seatsBooked) {
        this.tripId = tripId;
        this.seatsBooked = seatsBooked;
    }

    // Getters and Setters
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public Integer getSeatsBooked() { return seatsBooked; }
    public void setSeatsBooked(Integer seatsBooked) { this.seatsBooked = seatsBooked; }

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
}