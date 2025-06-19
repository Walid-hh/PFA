package com.covoituragedigitalise.trip.repository;

import com.covoituragedigitalise.trip.entity.Trip;
import com.covoituragedigitalise.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // Recherche de trajets par conducteur
    List<Trip> findByDriverId(Long driverId);

    // Recherche de trajets actifs par conducteur
    List<Trip> findByDriverIdAndTripStatus(Long driverId, TripStatus tripStatus);

    // Recherche de trajets par statut
    List<Trip> findByTripStatus(TripStatus tripStatus);

    // Recherche de trajets avec places disponibles
    @Query("SELECT t FROM Trip t WHERE t.availableSeats > 0 AND t.tripStatus = 'ACTIVE'")
    List<Trip> findAvailableTrips();

    // Recherche de trajets par ville de départ et d'arrivée
    @Query("SELECT t FROM Trip t WHERE " +
            "LOWER(t.departureLocation) LIKE LOWER(CONCAT('%', :departureLocation, '%')) AND " +
            "LOWER(t.arrivalLocation) LIKE LOWER(CONCAT('%', :arrivalLocation, '%')) AND " +
            "t.tripStatus = 'ACTIVE' AND t.availableSeats > 0")
    List<Trip> findTripsByLocations(@Param("departureLocation") String departureLocation,
                                    @Param("arrivalLocation") String arrivalLocation);

    // Recherche de trajets par date
    @Query("SELECT t FROM Trip t WHERE " +
            "DATE(t.departureTime) = DATE(:departureDate) AND " +
            "t.tripStatus = 'ACTIVE' AND t.availableSeats > 0")
    List<Trip> findTripsByDepartureDate(@Param("departureDate") LocalDateTime departureDate);

    // Recherche complète (ville + date)
    @Query("SELECT t FROM Trip t WHERE " +
            "LOWER(t.departureLocation) LIKE LOWER(CONCAT('%', :departureLocation, '%')) AND " +
            "LOWER(t.arrivalLocation) LIKE LOWER(CONCAT('%', :arrivalLocation, '%')) AND " +
            "DATE(t.departureTime) = DATE(:departureDate) AND " +
            "t.tripStatus = 'ACTIVE' AND t.availableSeats > 0 " +
            "ORDER BY t.departureTime ASC")
    List<Trip> findTripsByLocationsAndDate(@Param("departureLocation") String departureLocation,
                                           @Param("arrivalLocation") String arrivalLocation,
                                           @Param("departureDate") LocalDateTime departureDate);

    // Recherche flexible par lieux avec tri par prix
    @Query("SELECT t FROM Trip t WHERE " +
            "LOWER(t.departureLocation) LIKE LOWER(CONCAT('%', :departureLocation, '%')) AND " +
            "LOWER(t.arrivalLocation) LIKE LOWER(CONCAT('%', :arrivalLocation, '%')) AND " +
            "t.tripStatus = 'ACTIVE' AND t.availableSeats >= :minSeats " +
            "ORDER BY t.pricePerSeat ASC")
    List<Trip> findTripsByLocationsOrderByPrice(@Param("departureLocation") String departureLocation,
                                                @Param("arrivalLocation") String arrivalLocation,
                                                @Param("minSeats") Integer minSeats);

    // Recherche par proximité géographique (si coordonnées disponibles)
    @Query("SELECT t FROM Trip t WHERE " +
            "t.departureLatitude IS NOT NULL AND t.departureLongitude IS NOT NULL AND " +
            "t.arrivalLatitude IS NOT NULL AND t.arrivalLongitude IS NOT NULL AND " +
            "(6371 * acos(cos(radians(:depLat)) * cos(radians(t.departureLatitude)) * " +
            "cos(radians(t.departureLongitude) - radians(:depLng)) + " +
            "sin(radians(:depLat)) * sin(radians(t.departureLatitude)))) < :maxDistance AND " +
            "t.tripStatus = 'ACTIVE' AND t.availableSeats > 0")
    List<Trip> findTripsByProximity(@Param("depLat") Double departureLatitude,
                                    @Param("depLng") Double departureLongitude,
                                    @Param("maxDistance") Double maxDistanceKm);

    // Trajets expirant bientôt (pour nettoyage automatique)
    @Query("SELECT t FROM Trip t WHERE t.departureTime < :cutoffTime AND t.tripStatus = 'ACTIVE'")
    List<Trip> findExpiredTrips(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Statistiques - Compter les trajets par conducteur
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driverId = :driverId")
    Long countTripsByDriver(@Param("driverId") Long driverId);

    // Statistiques - Compter les trajets complétés par conducteur
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.driverId = :driverId AND t.tripStatus = 'COMPLETED'")
    Long countCompletedTripsByDriver(@Param("driverId") Long driverId);

    // Vérifier si un conducteur a déjà un trajet à cette heure - CORRIGÉ
    @Query("SELECT t FROM Trip t WHERE t.driverId = :driverId AND " +
            "t.departureTime BETWEEN :startTime AND :endTime AND " +
            "t.tripStatus = 'ACTIVE'")
    Optional<Trip> findConflictingTrip(@Param("driverId") Long driverId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
}