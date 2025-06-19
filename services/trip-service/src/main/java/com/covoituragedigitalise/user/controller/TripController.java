package com.covoituragedigitalise.trip.controller;

import com.covoituragedigitalise.trip.entity.Trip;
import com.covoituragedigitalise.trip.service.JwtService;
import com.covoituragedigitalise.trip.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @Autowired
    private TripService tripService;

    @Autowired
    private JwtService jwtService;

    // ✅ Health Check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Trip Service",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }

    // ✅ CRÉER un nouveau trajet (conducteur uniquement)
    @PostMapping
    public ResponseEntity<?> createTrip(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> tripData) {
        try {
            System.out.println("🚗 TRIP - CREATE - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID et vérifier que c'est un conducteur
            // Pour l'instant, on utilise un ID fictif
            Long driverId = 1L; // À remplacer par l'appel au User Service

            Trip trip = tripService.createTrip(driverId, tripData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 201);
            response.put("message", "Trajet créé avec succès");
            response.put("trip", formatTripResponse(trip));

            System.out.println("✅ TRIP - CREATE - Succès");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - CREATE - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ RECHERCHER des trajets
    @GetMapping("/search")
    public ResponseEntity<?> searchTrips(@RequestParam Map<String, Object> searchParams) {
        try {
            System.out.println("🔍 TRIP - SEARCH - Critères: " + searchParams);

            List<Trip> trips = tripService.searchTrips(searchParams);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", trips.size() + " trajet(s) trouvé(s)");
            response.put("trips", trips.stream().map(this::formatTripResponse).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - SEARCH - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les détails d'un trajet
    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTripDetails(@PathVariable Long tripId) {
        try {
            System.out.println("🔍 TRIP - GET DETAILS - ID: " + tripId);

            Trip trip = tripService.getTripById(tripId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("trip", formatTripResponse(trip));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - GET DETAILS - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les trajets du conducteur connecté
    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyTrips(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("🔍 TRIP - MY TRIPS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long driverId = 1L; // À remplacer

            List<Trip> trips = tripService.getDriverTrips(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", trips.size() + " trajet(s) trouvé(s)");
            response.put("trips", trips.stream().map(this::formatTripResponse).toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - MY TRIPS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // ✅ MODIFIER un trajet
    @PutMapping("/{tripId}")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> updateData) {
        try {
            System.out.println("🔄 TRIP - UPDATE - ID: " + tripId);

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long driverId = 1L; // À remplacer

            Trip trip = tripService.updateTrip(tripId, driverId, updateData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Trajet modifié avec succès");
            response.put("trip", formatTripResponse(trip));

            System.out.println("✅ TRIP - UPDATE - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - UPDATE - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ ANNULER un trajet
    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> cancelTrip(
            @PathVariable Long tripId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("❌ TRIP - CANCEL - ID: " + tripId);

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long driverId = 1L; // À remplacer

            tripService.cancelTrip(tripId, driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Trajet annulé avec succès");

            System.out.println("✅ TRIP - CANCEL - Succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - CANCEL - Erreur: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("status", 400, "message", e.getMessage()));
        }
    }

    // ✅ OBTENIR les statistiques du conducteur
    @GetMapping("/stats")
    public ResponseEntity<?> getDriverStats(@RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("📊 TRIP - STATS - Début");

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            // TODO: Appeler User Service pour obtenir l'ID
            Long driverId = 1L; // À remplacer

            Map<String, Object> stats = tripService.getDriverStats(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ TRIP - STATS - Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Token invalide"));
        }
    }

    // 🔧 Méthode privée pour formater la réponse d'un trajet
    private Map<String, Object> formatTripResponse(Trip trip) {
        Map<String, Object> response = new HashMap<>();
        response.put("tripId", trip.getId());
        response.put("driverId", trip.getDriverId());
        response.put("departureLocation", trip.getDepartureLocation());
        response.put("arrivalLocation", trip.getArrivalLocation());
        response.put("departureTime", trip.getDepartureTime().toString());
        response.put("availableSeats", trip.getAvailableSeats());
        response.put("originalSeats", trip.getOriginalSeats());
        response.put("pricePerSeat", trip.getPricePerSeat());
        response.put("description", trip.getDescription());
        response.put("tripStatus", trip.getTripStatus().toString());
        response.put("isInstantBooking", trip.getIsInstantBooking());
        response.put("createdAt", trip.getCreatedAt().toString());

        // Informations sur les réservations
        response.put("totalBookings", trip.getBookings().size());
        response.put("confirmedBookings", trip.getBookings().stream()
                .mapToInt(b -> b.isConfirmed() ? 1 : 0).sum());

        return response;
    }
}