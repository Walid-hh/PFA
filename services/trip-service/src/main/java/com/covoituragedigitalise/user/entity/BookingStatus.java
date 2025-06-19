package com.covoituragedigitalise.trip.entity;

public enum BookingStatus {
    PENDING("En attente"),
    CONFIRMED("Confirmée"),
    REJECTED("Refusée"),
    CANCELLED("Annulée"),
    COMPLETED("Terminée");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}