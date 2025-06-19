package com.covoituragedigitalise.trip.service;

import com.covoituragedigitalise.trip.entity.Booking;
import com.covoituragedigitalise.trip.entity.BookingStatus;
import com.covoituragedigitalise.trip.entity.Trip;
import com.covoituragedigitalise.trip.entity.TripStatus;
import com.covoituragedigitalise.trip.repository.BookingRepository;
import com.covoituragedigitalise.trip.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripService tripService;

    // ✅ CRÉER une nouvelle réservation
    public Booking createBooking(Long passengerId, Map<String, Object> bookingData) {
        try {
            System.out.println("📝 BookingService - Création réservation pour passager: " + passengerId);

            // Récupérer les données
            Long tripId = Long.valueOf(bookingData.get("tripId").toString());
            Integer seatsRequested = (Integer) bookingData.get("seatsBooked");

            if (seatsRequested == null || seatsRequested <= 0) {
                throw new IllegalArgumentException("Nombre de places doit être positif");
            }

            // Récupérer le trajet
            Trip trip = tripService.getTripById(tripId);

            // Validations
            validateBooking(trip, passengerId, seatsRequested);

            // Vérifier si le passager n'a pas déjà réservé ce trajet
            Optional<Booking> existingBooking = bookingRepository.findByTripAndPassengerId(trip, passengerId);
            if (existingBooking.isPresent()) {
                throw new IllegalArgumentException("Vous avez déjà une réservation pour ce trajet");
            }

            // Calculer le prix total
            BigDecimal totalPrice = trip.calculateTotalPrice(seatsRequested);

            // Créer la réservation
            Booking booking = new Booking(trip, passengerId, seatsRequested, totalPrice);

            // Ajouter les informations optionnelles
            if (bookingData.containsKey("pickupLocation")) {
                booking.setPickupLocation((String) bookingData.get("pickupLocation"));
            }
            if (bookingData.containsKey("dropoffLocation")) {
                booking.setDropoffLocation((String) bookingData.get("dropoffLocation"));
            }
            if (bookingData.containsKey("specialRequests")) {
                booking.setSpecialRequests((String) bookingData.get("specialRequests"));
            }
            if (bookingData.containsKey("passengerPhone")) {
                booking.setPassengerPhone((String) bookingData.get("passengerPhone"));
            }
            if (bookingData.containsKey("passengerName")) {
                booking.setPassengerName((String) bookingData.get("passengerName"));
            }

            // Si c'est une réservation instantanée, confirmer directement
            if (trip.getIsInstantBooking()) {
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                trip.reserveSeats(seatsRequested);
            }

            Booking savedBooking = bookingRepository.save(booking);

            // Sauvegarder le trajet si les places ont été réservées
            if (trip.getIsInstantBooking()) {
                tripRepository.save(trip);
            }

            System.out.println("✅ BookingService - Réservation créée: " + savedBooking.getId());
            return savedBooking;

        } catch (Exception e) {
            System.err.println("❌ BookingService - Erreur createBooking: " + e.getMessage());
            throw e;
        }
    }

    // ✅ OBTENIR les réservations d'un passager
    public List<Booking> getPassengerBookings(Long passengerId) {
        System.out.println("🔍 BookingService - Réservations du passager: " + passengerId);
        return bookingRepository.findByPassengerId(passengerId);
    }

    // ✅ OBTENIR les réservations pour un conducteur
    public List<Booking> getDriverBookings(Long driverId) {
        System.out.println("🔍 BookingService - Réservations pour conducteur: " + driverId);
        return bookingRepository.findBookingsForDriver(driverId);
    }

    // ✅ OBTENIR les réservations en attente pour un conducteur
    public List<Booking> getPendingBookingsForDriver(Long driverId) {
        System.out.println("⏳ BookingService - Réservations en attente pour conducteur: " + driverId);
        return bookingRepository.findPendingBookingsForDriver(driverId);
    }

    // ✅ CONFIRMER une réservation (par le conducteur)
    public Booking confirmBooking(Long bookingId, Long driverId) {
        try {
            System.out.println("✅ BookingService - Confirmation réservation: " + bookingId);

            Booking booking = getBookingById(bookingId);
            Trip trip = booking.getTrip();

            // Vérifier que c'est le bon conducteur
            if (!trip.getDriverId().equals(driverId)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à confirmer cette réservation");
            }

            // Vérifier que la réservation peut être confirmée
            if (!booking.canBeConfirmed()) {
                throw new IllegalArgumentException("Cette réservation ne peut pas être confirmée");
            }

            // Vérifier qu'il y a encore assez de places
            if (!trip.canBook(booking.getSeatsBooked())) {
                throw new IllegalArgumentException("Plus assez de places disponibles");
            }

            // Confirmer la réservation et réserver les places
            booking.confirm();
            trip.reserveSeats(booking.getSeatsBooked());

            // Sauvegarder
            bookingRepository.save(booking);
            tripRepository.save(trip);

            System.out.println("✅ BookingService - Réservation confirmée: " + bookingId);
            return booking;

        } catch (Exception e) {
            System.err.println("❌ BookingService - Erreur confirmBooking: " + e.getMessage());
            throw e;
        }
    }

    // ✅ REJETER une réservation (par le conducteur)
    public Booking rejectBooking(Long bookingId, Long driverId) {
        try {
            System.out.println("❌ BookingService - Rejet réservation: " + bookingId);

            Booking booking = getBookingById(bookingId);
            Trip trip = booking.getTrip();

            // Vérifier que c'est le bon conducteur
            if (!trip.getDriverId().equals(driverId)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à rejeter cette réservation");
            }

            // Rejeter la réservation
            booking.reject();
            Booking savedBooking = bookingRepository.save(booking);

            System.out.println("✅ BookingService - Réservation rejetée: " + bookingId);
            return savedBooking;

        } catch (Exception e) {
            System.err.println("❌ BookingService - Erreur rejectBooking: " + e.getMessage());
            throw e;
        }
    }

    // ✅ ANNULER une réservation (par le passager)
    public Booking cancelBooking(Long bookingId, Long passengerId) {
        try {
            System.out.println("❌ BookingService - Annulation réservation: " + bookingId);

            Booking booking = getBookingById(bookingId);

            // Vérifier que c'est le bon passager
            if (!booking.getPassengerId().equals(passengerId)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à annuler cette réservation");
            }

            // Vérifier que la réservation peut être annulée
            if (!booking.canBeCancelled()) {
                throw new IllegalArgumentException("Cette réservation ne peut pas être annulée");
            }

            // Si la réservation était confirmée, libérer les places
            if (booking.isConfirmed()) {
                Trip trip = booking.getTrip();
                trip.releaseSeats(booking.getSeatsBooked());
                tripRepository.save(trip);
            }

            // Annuler la réservation
            booking.cancel();
            Booking savedBooking = bookingRepository.save(booking);

            System.out.println("✅ BookingService - Réservation annulée: " + bookingId);
            return savedBooking;

        } catch (Exception e) {
            System.err.println("❌ BookingService - Erreur cancelBooking: " + e.getMessage());
            throw e;
        }
    }

    // ✅ OBTENIR une réservation par ID
    public Booking getBookingById(Long bookingId) {
        System.out.println("🔍 BookingService - Recherche réservation: " + bookingId);
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée: " + bookingId));
    }

    // ✅ OBTENIR les statistiques d'un passager
    public Map<String, Object> getPassengerStats(Long passengerId) {
        Long totalBookings = bookingRepository.countBookingsByPassenger(passengerId);
        Long confirmedBookings = bookingRepository.countConfirmedBookingsByPassenger(passengerId);
        Double totalExpenses = bookingRepository.calculatePassengerExpenses(passengerId);

        return Map.of(
                "totalBookings", totalBookings,
                "confirmedBookings", confirmedBookings,
                "totalExpenses", totalExpenses != null ? totalExpenses : 0.0,
                "activeBookings", bookingRepository.findActiveBookingsByPassenger(passengerId).size()
        );
    }

    // ✅ OBTENIR les revenus d'un conducteur
    public Map<String, Object> getDriverEarnings(Long driverId) {
        Double totalEarnings = bookingRepository.calculateDriverEarnings(driverId);
        List<Booking> confirmedBookings = bookingRepository.findBookingsForDriver(driverId)
                .stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .toList();

        return Map.of(
                "totalEarnings", totalEarnings != null ? totalEarnings : 0.0,
                "totalConfirmedBookings", confirmedBookings.size(),
                "pendingBookings", bookingRepository.findPendingBookingsForDriver(driverId).size()
        );
    }

    // 🔧 Méthodes privées de validation
    private void validateBooking(Trip trip, Long passengerId, Integer seatsRequested) {
        // Vérifier que le trajet est actif
        if (!trip.isActive()) {
            throw new IllegalArgumentException("Ce trajet n'est plus disponible");
        }

        // Vérifier que le trajet est dans le futur
        if (!trip.isDepartureInFuture()) {
            throw new IllegalArgumentException("Ce trajet est déjà parti");
        }

        // Vérifier que le passager n'est pas le conducteur
        if (trip.getDriverId().equals(passengerId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas réserver votre propre trajet");
        }

        // Vérifier qu'il y a assez de places
        if (!trip.canBook(seatsRequested)) {
            throw new IllegalArgumentException("Plus assez de places disponibles (" + trip.getAvailableSeats() + " restantes)");
        }

        // Vérifier les conflits d'horaire (±2 heures)
        LocalDateTime startTime = trip.getDepartureTime().minusHours(2);
        LocalDateTime endTime = trip.getDepartureTime().plusHours(2);
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(passengerId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Vous avez déjà une réservation à cette heure");
        }
    }
}