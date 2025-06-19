package com.covoituragedigitalise.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TripCreateDto {

    @NotBlank(message = "Lieu de départ obligatoire")
    private String departureLocation;

    @NotBlank(message = "Lieu d'arrivée obligatoire")
    private String arrivalLocation;

    @NotNull(message = "Heure de départ obligatoire")
    private LocalDateTime departureTime;

    @NotNull(message = "Nombre de places obligatoire")
    @Positive(message = "Le nombre de places doit être positif")
    private Integer availableSeats;

    @NotNull(message = "Prix par place obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal pricePerSeat;

    private String description;
    private Boolean isInstantBooking = false;
    private Double departureLatitude;
    private Double departureLongitude;
    private Double arrivalLatitude;
    private Double arrivalLongitude;

    // Constructors
    public TripCreateDto() {}

    public TripCreateDto(String departureLocation, String arrivalLocation,
                         LocalDateTime departureTime, Integer availableSeats, BigDecimal pricePerSeat) {
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
    }

    // Getters and Setters
    public String getDepartureLocation() { return departureLocation; }
    public void setDepartureLocation(String departureLocation) { this.departureLocation = departureLocation; }

    public String getArrivalLocation() { return arrivalLocation; }
    public void setArrivalLocation(String arrivalLocation) { this.arrivalLocation = arrivalLocation; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public BigDecimal getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(BigDecimal pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsInstantBooking() { return isInstantBooking; }
    public void setIsInstantBooking(Boolean isInstantBooking) { this.isInstantBooking = isInstantBooking; }

    public Double getDepartureLatitude() { return departureLatitude; }
    public void setDepartureLatitude(Double departureLatitude) { this.departureLatitude = departureLatitude; }

    public Double getDepartureLongitude() { return departureLongitude; }
    public void setDepartureLongitude(Double departureLongitude) { this.departureLongitude = departureLongitude; }

    public Double getArrivalLatitude() { return arrivalLatitude; }
    public void setArrivalLatitude(Double arrivalLatitude) { this.arrivalLatitude = arrivalLatitude; }

    public Double getArrivalLongitude() { return arrivalLongitude; }
    public void setArrivalLongitude(Double arrivalLongitude) { this.arrivalLongitude = arrivalLongitude; }
}