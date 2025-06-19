package com.covoituragedigitalise.trip.entity;

public enum TripStatus {
    ACTIVE("Actif"),
    FULL("Complet"),
    CANCELLED("Annulé"),
    COMPLETED("Terminé"),
    EXPIRED("Expiré");

    private final String displayName;

    TripStatus(String displayName) {
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