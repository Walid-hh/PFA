package com.covoituragedigitalise.trip.repository;

import com.covoituragedigitalise.trip.entity.Booking;
import com.covoituragedigitalise.trip.entity.BookingStatus;
import com.covoituragedigitalise.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Recherche de réservations par passager
    List<Booking> findByPassengerId(Long passengerId);

    // Recherche de réservations par passager et statut
    List<Booking> findByPassengerIdAndBookingStatus(Long passengerId, BookingStatus bookingStatus);

    // Recherche de réservations par trajet
    List<Booking> findByTrip(Trip trip);

    // Recherche de réservations par trajet et statut
    List<Booking> findByTripAndBookingStatus(Trip trip, BookingStatus bookingStatus);

    // Recherche de réservations par ID de trajet
    List<Booking> findByTripId(Long tripId);

    // Vérifier si un passager a déjà réservé ce trajet
    Optional<Booking> findByTripAndPassengerId(Trip trip, Long passengerId);

    // Recherche de réservations actives d'un passager
    @Query("SELECT b FROM Booking b WHERE b.passengerId = :passengerId AND " +
            "b.bookingStatus IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findActiveBookingsByPassenger(@Param("passengerId") Long passengerId);

    // Recherche de réservations pour un conducteur (via ses trajets)
    @Query("SELECT b FROM Booking b WHERE b.trip.driverId = :driverId " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findBookingsForDriver(@Param("driverId") Long driverId);

    // Recherche de réservations en attente pour un conducteur
    @Query("SELECT b FROM Booking b WHERE b.trip.driverId = :driverId AND " +
            "b.bookingStatus = 'PENDING' " +
            "ORDER BY b.createdAt ASC")
    List<Booking> findPendingBookingsForDriver(@Param("driverId") Long driverId);

    // Recherche de réservations confirmées pour un trajet
    @Query("SELECT b FROM Booking b WHERE b.trip.id = :tripId AND " +
            "b.bookingStatus = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByTrip(@Param("tripId") Long tripId);

    // Compter les places réservées pour un trajet
    @Query("SELECT COALESCE(SUM(b.seatsBooked), 0) FROM Booking b WHERE " +
            "b.trip.id = :tripId AND b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    Integer countBookedSeatsByTrip(@Param("tripId") Long tripId);

    // Recherche de réservations par date de voyage
    @Query("SELECT b FROM Booking b WHERE " +
            "DATE(b.trip.departureTime) = DATE(:departureDate) AND " +
            "b.passengerId = :passengerId AND " +
            "b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    List<Booking> findBookingsByPassengerAndDate(@Param("passengerId") Long passengerId,
                                                 @Param("departureDate") LocalDateTime departureDate);

    // Recherche de réservations récentes
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :since " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings(@Param("since") LocalDateTime since);

    // Statistiques - Compter réservations par passager
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.passengerId = :passengerId")
    Long countBookingsByPassenger(@Param("passengerId") Long passengerId);

    // Statistiques - Compter réservations confirmées par passager
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.passengerId = :passengerId AND " +
            "b.bookingStatus = 'CONFIRMED'")
    Long countConfirmedBookingsByPassenger(@Param("passengerId") Long passengerId);

    // Recherche de conflits de réservation (même passager, même heure) - CORRIGÉ
    @Query("SELECT b FROM Booking b WHERE b.passengerId = :passengerId AND " +
            "b.trip.departureTime BETWEEN :startTime AND :endTime AND " +
            "b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    List<Booking> findConflictingBookings(@Param("passengerId") Long passengerId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    // Recherche des revenus d'un conducteur
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE " +
            "b.trip.driverId = :driverId AND b.bookingStatus = 'CONFIRMED'")
    Double calculateDriverEarnings(@Param("driverId") Long driverId);

    // Recherche des dépenses d'un passager
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE " +
            "b.passengerId = :passengerId AND b.bookingStatus = 'CONFIRMED'")
    Double calculatePassengerExpenses(@Param("passengerId") Long passengerId);
}