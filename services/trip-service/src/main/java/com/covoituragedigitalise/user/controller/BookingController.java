package com.covoituragedigitalise.trip.controller;

import com.covoituragedigitalise.trip.entity.Booking;
import com.covoituragedigitalise.trip.service.BookingService;
import com.covoituragedigitalise.trip.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtService jwtService;

    // ✅ Health Check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Booking Service",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }

    // ✅ CRÉER une nouvelle réservation
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> bookingData) {
        try {
            System.out.println("📝 BOOKING - CREATE - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID du passager
            Long passengerId = 2L; // À remplacer par l'appel au User Service

            Booking booking = bookingService.createBooking(passengerId, bookingData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 201);
            response.put("message", "Réservation créée avec succès");
            response.put("booking", formatBookingResponse(booking));

            System.out.println("✅ BOOKING - CREATE - Succès");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - CREATE - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les réservations du passager connecté
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🔍 BOOKING - MY BOOKINGS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long passengerId = 2L; // À remplacer

            List<Booking> bookings = bookingService.getPassengerBookings(passengerId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", bookings.size() + " réservation(s) trouvée(s)");
            response.put("bookings", bookings.stream().map(this::formatBookingResponse).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - MY BOOKINGS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // ✅ OBTENIR les réservations pour les trajets du conducteur
    @GetMapping("/driver-bookings")
    public ResponseEntity<?> getDriverBookings(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🔍 BOOKING - DRIVER BOOKINGS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            Long driverId = 1L; // À remplacer

            List<Booking> bookings = bookingService.getDriverBookings(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", bookings.size() + " réservation(s) trouvée(s)");
            response.put("bookings", bookings.stream().map(this::formatBookingResponse).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - DRIVER BOOKINGS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // ✅ OBTENIR les réservations en attente pour le conducteur
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingBookings(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("⏳ BOOKING - PENDING - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            Long driverId = 1L; // À remplacer

            List<Booking> bookings = bookingService.getPendingBookingsForDriver(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", bookings.size() + " réservation(s) en attente");
            response.put("bookings", bookings.stream().map(this::formatBookingResponse).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - PENDING - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // ✅ CONFIRMER une réservation (conducteur)
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("✅ BOOKING - CONFIRM - ID: " + bookingId);

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            Long driverId = 1L; // À remplacer

            Booking booking = bookingService.confirmBooking(bookingId, driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Réservation confirmée avec succès");
            response.put("booking", formatBookingResponse(booking));

            System.out.println("✅ BOOKING - CONFIRM - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - CONFIRM - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ REJETER une réservation (conducteur)
    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<?> rejectBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("❌ BOOKING - REJECT - ID: " + bookingId);

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            Long driverId = 1L; // À remplacer

            Booking booking = bookingService.rejectBooking(bookingId, driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Réservation rejetée");
            response.put("booking", formatBookingResponse(booking));

            System.out.println("✅ BOOKING - REJECT - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - REJECT - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ ANNULER une réservation (passager)
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("❌ BOOKING - CANCEL - ID: " + bookingId);

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long passengerId = 2L; // À remplacer

            Booking booking = bookingService.cancelBooking(bookingId, passengerId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Réservation annulée avec succès");
            response.put("booking", formatBookingResponse(booking));

            System.out.println("✅ BOOKING - CANCEL - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - CANCEL - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les détails d'une réservation
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetails(@PathVariable Long bookingId) {
        try {
            System.out.println("🔍 BOOKING - GET DETAILS - ID: " + bookingId);

            Booking booking = bookingService.getBookingById(bookingId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("booking", formatBookingResponse(booking));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - GET DETAILS - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les statistiques du passager
    @GetMapping("/passenger-stats")
    public ResponseEntity<?> getPassengerStats(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("📊 BOOKING - PASSENGER STATS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long passengerId = 2L; // À remplacer

            Map<String, Object> stats = bookingService.getPassengerStats(passengerId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - PASSENGER STATS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // ✅ OBTENIR les revenus du conducteur
    @GetMapping("/driver-earnings")
    public ResponseEntity<?> getDriverEarnings(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("💰 BOOKING - DRIVER EARNINGS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            Long driverId = 1L; // À remplacer

            Map<String, Object> earnings = bookingService.getDriverEarnings(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("earnings", earnings);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ BOOKING - DRIVER EARNINGS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // 🔧 Méthode privée pour formater la réponse d'une réservation
    private Map<String, Object> formatBookingResponse(Booking booking) {
        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        response.put("tripId", booking.getTrip().getId());
        response.put("passengerId", booking.getPassengerId());
        response.put("seatsBooked", booking.getSeatsBooked());
        response.put("totalPrice", booking.getTotalPrice());
        response.put("bookingStatus", booking.getBookingStatus().toString());
        response.put("bookingDate", booking.getBookingDate().toString());
        response.put("pickupLocation", booking.getPickupLocation());
        response.put("dropoffLocation", booking.getDropoffLocation());
        response.put("specialRequests", booking.getSpecialRequests());
        response.put("passengerPhone", booking.getPassengerPhone());
        response.put("passengerName", booking.getPassengerName());
        response.put("createdAt", booking.getCreatedAt().toString());

        // Informations sur le trajet
        Map<String, Object> tripInfo = new HashMap<>();
        tripInfo.put("departureLocation", booking.getTrip().getDepartureLocation());
        tripInfo.put("arrivalLocation", booking.getTrip().getArrivalLocation());
        tripInfo.put("departureTime", booking.getTrip().getDepartureTime().toString());
        tripInfo.put("driverId", booking.getTrip().getDriverId());
        response.put("trip", tripInfo);

        return response;
    }
}