package com.covoituragedigitalise.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TripSearchDto {

    private String departureLocation;
    private String arrivalLocation;
    private LocalDate departureDate;
    private Integer minAvailableSeats = 1;
    private BigDecimal maxPricePerSeat;
    private Boolean instantBookingOnly = false;
    private String sortBy = "departureTime"; // departureTime, price, seats
    private String sortDirection = "ASC"; // ASC, DESC

    // Constructors
    public TripSearchDto() {}

    public TripSearchDto(String departureLocation, String arrivalLocation, LocalDate departureDate) {
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
        this.departureDate = departureDate;
    }

    // Getters and Setters
    public String getDepartureLocation() { return departureLocation; }
    public void setDepartureLocation(String departureLocation) { this.departureLocation = departureLocation; }

    public String getArrivalLocation() { return arrivalLocation; }
    public void setArrivalLocation(String arrivalLocation) { this.arrivalLocation = arrivalLocation; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public Integer getMinAvailableSeats() { return minAvailableSeats; }
    public void setMinAvailableSeats(Integer minAvailableSeats) { this.minAvailableSeats = minAvailableSeats; }

    public BigDecimal getMaxPricePerSeat() { return maxPricePerSeat; }
    public void setMaxPricePerSeat(BigDecimal maxPricePerSeat) { this.maxPricePerSeat = maxPricePerSeat; }

    public Boolean getInstantBookingOnly() { return instantBookingOnly; }
    public void setInstantBookingOnly(Boolean instantBookingOnly) { this.instantBookingOnly = instantBookingOnly; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}