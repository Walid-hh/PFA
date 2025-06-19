package com.covoituragedigitalise.trip.service;

import com.covoituragedigitalise.trip.entity.Trip;
import com.covoituragedigitalise.trip.entity.TripStatus;
import com.covoituragedigitalise.trip.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    // ✅ CRÉER un nouveau trajet
    public Trip createTrip(Long driverId, Map<String, Object> tripData) {
        try {
            System.out.println("🚗 TripService - Création trajet pour conducteur: " + driverId);

            // Vérifier les données obligatoires
            validateTripData(tripData);

            // Créer le trajet
            Trip trip = new Trip();
            trip.setDriverId(driverId);
            trip.setDepartureLocation((String) tripData.get("departureLocation"));
            trip.setArrivalLocation((String) tripData.get("arrivalLocation"));

            // Parser la date de départ
            String departureTimeStr = (String) tripData.get("departureTime");
            trip.setDepartureTime(LocalDateTime.parse(departureTimeStr));

            Integer seats = (Integer) tripData.get("availableSeats");
            trip.setAvailableSeats(seats);
            trip.setOriginalSeats(seats);

            trip.setPricePerSeat(new java.math.BigDecimal(tripData.get("pricePerSeat").toString()));

            // Données optionnelles
            if (tripData.containsKey("description")) {
                trip.setDescription((String) tripData.get("description"));
            }
            if (tripData.containsKey("isInstantBooking")) {
                trip.setIsInstantBooking((Boolean) tripData.get("isInstantBooking"));
            }

            // Vérifier les conflits d'horaire (±1 heure)
            LocalDateTime startTime = trip.getDepartureTime().minusHours(1);
            LocalDateTime endTime = trip.getDepartureTime().plusHours(1);
            Optional<Trip> conflictingTrip = tripRepository.findConflictingTrip(driverId, startTime, endTime);
            if (conflictingTrip.isPresent()) {
                throw new IllegalArgumentException("Vous avez déjà un trajet prévu à cette heure");
            }

            Trip savedTrip = tripRepository.save(trip);
            System.out.println("✅ TripService - Trajet créé: " + savedTrip.getId());

            return savedTrip;

        } catch (Exception e) {
            System.err.println("❌ TripService - Erreur createTrip: " + e.getMessage());
            throw e;
        }
    }

    // ✅ RECHERCHER des trajets
    public List<Trip> searchTrips(Map<String, Object> searchCriteria) {
        try {
            System.out.println("🔍 TripService - Recherche trajets: " + searchCriteria);

            String departureLocation = (String) searchCriteria.get("departureLocation");
            String arrivalLocation = (String) searchCriteria.get("arrivalLocation");
            String departureDateStr = (String) searchCriteria.get("departureDate");

            // Recherche par lieux uniquement
            if (departureLocation != null && arrivalLocation != null && departureDateStr == null) {
                return tripRepository.findTripsByLocations(departureLocation, arrivalLocation);
            }

            // Recherche par lieux et date
            if (departureLocation != null && arrivalLocation != null && departureDateStr != null) {
                LocalDateTime departureDate = LocalDateTime.parse(departureDateStr + "T00:00:00");
                return tripRepository.findTripsByLocationsAndDate(departureLocation, arrivalLocation, departureDate);
            }

            // Recherche par date uniquement
            if (departureDateStr != null) {
                LocalDateTime departureDate = LocalDateTime.parse(departureDateStr + "T00:00:00");
                return tripRepository.findTripsByDepartureDate(departureDate);
            }

            // Par défaut, retourner tous les trajets disponibles
            return tripRepository.findAvailableTrips();

        } catch (Exception e) {
            System.err.println("❌ TripService - Erreur searchTrips: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche de trajets", e);
        }
    }

    // ✅ OBTENIR un trajet par ID
    public Trip getTripById(Long tripId) {
        System.out.println("🔍 TripService - Recherche trajet: " + tripId);
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé: " + tripId));
    }

    // ✅ OBTENIR les trajets d'un conducteur
    public List<Trip> getDriverTrips(Long driverId) {
        System.out.println("🔍 TripService - Trajets du conducteur: " + driverId);
        return tripRepository.findByDriverId(driverId);
    }

    // ✅ MODIFIER un trajet
    public Trip updateTrip(Long tripId, Long driverId, Map<String, Object> updateData) {
        try {
            System.out.println("🔄 TripService - Modification trajet: " + tripId);

            Trip trip = getTripById(tripId);

            // Vérifier que le conducteur est propriétaire du trajet
            if (!trip.getDriverId().equals(driverId)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce trajet");
            }

            // Vérifier que le trajet peut être modifié
            if (!trip.isActive()) {
                throw new IllegalArgumentException("Ce trajet ne peut plus être modifié");
            }

            // Mettre à jour les champs autorisés
            if (updateData.containsKey("departureLocation")) {
                trip.setDepartureLocation((String) updateData.get("departureLocation"));
            }
            if (updateData.containsKey("arrivalLocation")) {
                trip.setArrivalLocation((String) updateData.get("arrivalLocation"));
            }
            if (updateData.containsKey("departureTime")) {
                String departureTimeStr = (String) updateData.get("departureTime");
                trip.setDepartureTime(LocalDateTime.parse(departureTimeStr));
            }
            if (updateData.containsKey("pricePerSeat")) {
                trip.setPricePerSeat(new java.math.BigDecimal(updateData.get("pricePerSeat").toString()));
            }
            if (updateData.containsKey("description")) {
                trip.setDescription((String) updateData.get("description"));
            }

            Trip savedTrip = tripRepository.save(trip);
            System.out.println("✅ TripService - Trajet modifié: " + savedTrip.getId());

            return savedTrip;

        } catch (Exception e) {
            System.err.println("❌ TripService - Erreur updateTrip: " + e.getMessage());
            throw e;
        }
    }

    // ✅ ANNULER un trajet
    public void cancelTrip(Long tripId, Long driverId) {
        try {
            System.out.println("❌ TripService - Annulation trajet: " + tripId);

            Trip trip = getTripById(tripId);

            // Vérifier que le conducteur est propriétaire du trajet
            if (!trip.getDriverId().equals(driverId)) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à annuler ce trajet");
            }

            // Marquer comme annulé
            trip.setTripStatus(TripStatus.CANCELLED);
            tripRepository.save(trip);

            System.out.println("✅ TripService - Trajet annulé: " + tripId);

        } catch (Exception e) {
            System.err.println("❌ TripService - Erreur cancelTrip: " + e.getMessage());
            throw e;
        }
    }

    // ✅ METTRE À JOUR le statut d'un trajet
    public Trip updateTripStatus(Long tripId, TripStatus newStatus) {
        Trip trip = getTripById(tripId);
        trip.setTripStatus(newStatus);
        return tripRepository.save(trip);
    }

    // ✅ OBTENIR les statistiques d'un conducteur
    public Map<String, Object> getDriverStats(Long driverId) {
        Long totalTrips = tripRepository.countTripsByDriver(driverId);
        Long completedTrips = tripRepository.countCompletedTripsByDriver(driverId);

        return Map.of(
                "totalTrips", totalTrips,
                "completedTrips", completedTrips,
                "activeTrips", tripRepository.findByDriverIdAndTripStatus(driverId, TripStatus.ACTIVE).size()
        );
    }

    // 🔧 Méthode privée de validation
    private void validateTripData(Map<String, Object> tripData) {
        if (!tripData.containsKey("departureLocation") ||
                ((String) tripData.get("departureLocation")).trim().isEmpty()) {
            throw new IllegalArgumentException("Lieu de départ obligatoire");
        }
        if (!tripData.containsKey("arrivalLocation") ||
                ((String) tripData.get("arrivalLocation")).trim().isEmpty()) {
            throw new IllegalArgumentException("Lieu d'arrivée obligatoire");
        }
        if (!tripData.containsKey("departureTime")) {
            throw new IllegalArgumentException("Heure de départ obligatoire");
        }
        if (!tripData.containsKey("availableSeats") ||
                (Integer) tripData.get("availableSeats") <= 0) {
            throw new IllegalArgumentException("Nombre de places doit être positif");
        }
        if (!tripData.containsKey("pricePerSeat")) {
            throw new IllegalArgumentException("Prix par place obligatoire");
        }

        // Vérifier que la date de départ est dans le futur
        try {
            String departureTimeStr = (String) tripData.get("departureTime");
            LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
            if (departureTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de départ doit être dans le futur");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide. Utilisez: YYYY-MM-DDTHH:MM:SS");
        }
    }
}