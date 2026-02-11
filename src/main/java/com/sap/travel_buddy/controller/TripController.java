package com.sap.travel_buddy.controller;

import com.sap.travel_buddy.domain.Trip;
import com.sap.travel_buddy.dto.CreateTripRequest;
import com.sap.travel_buddy.dto.TripDto;
import com.sap.travel_buddy.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller за работа с разходки
 */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    /**
     * Създаване на нова разходка
     * POST /api/trips
     */
    @PostMapping
    public ResponseEntity<TripDto> createTrip(@RequestBody CreateTripRequest request) {
        log.info("Creating new trip: {}", request.getName());
        TripDto trip = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

    /**
     * Взимане на разходка по ID
     * GET /api/trips/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripDto> getTripById(@PathVariable Long id) {
        return tripService.getTripById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Взимане на всички разходки
     * GET /api/trips
     */
    @GetMapping
    public ResponseEntity<List<TripDto>> getAllTrips() {
        List<TripDto> trips = tripService.getAllTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * Взимане на предстоящи разходки
     * GET /api/trips/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<TripDto>> getUpcomingTrips() {
        List<TripDto> trips = tripService.getUpcomingTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * Взимане на препоръчани разходки
     * GET /api/trips/recommended
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<TripDto>> getRecommendedTrips() {
        List<TripDto> trips = tripService.getRecommendedTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * Взимане на разходки по статус
     * GET /api/trips/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TripDto>> getTripsByStatus(@PathVariable Trip.TripStatus status) {
        List<TripDto> trips = tripService.getTripsByStatus(status);
        return ResponseEntity.ok(trips);
    }

    /**
     * Търсене на разходки по име
     * GET /api/trips/search?name=разходка
     */
    @GetMapping("/search")
    public ResponseEntity<List<TripDto>> searchTripsByName(@RequestParam String name) {
        List<TripDto> trips = tripService.searchTripsByName(name);
        return ResponseEntity.ok(trips);
    }

    /**
     * Обновяване на статус на разходка
     * PATCH /api/trips/{id}/status?status=CONFIRMED
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TripDto> updateTripStatus(
            @PathVariable Long id,
            @RequestParam Trip.TripStatus status) {
        return tripService.updateTripStatus(id, status)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Добавяне на място към разходка
     * POST /api/trips/{tripId}/places/{placeId}
     */
    @PostMapping("/{tripId}/places/{placeId}")
    public ResponseEntity<TripDto> addPlaceToTrip(
            @PathVariable Long tripId,
            @PathVariable Long placeId) {
        return tripService.addPlaceToTrip(tripId, placeId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Премахване на място от разходка
     * DELETE /api/trips/{tripId}/places/{placeId}
     */
    @DeleteMapping("/{tripId}/places/{placeId}")
    public ResponseEntity<TripDto> removePlaceFromTrip(
            @PathVariable Long tripId,
            @PathVariable Long placeId) {
        return tripService.removePlaceFromTrip(tripId, placeId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Обновяване на прогнозата за разходка
     * POST /api/trips/{id}/refresh-weather
     */
    @PostMapping("/{id}/refresh-weather")
    public ResponseEntity<TripDto> refreshWeather(@PathVariable Long id) {
        return tripService.refreshWeatherForTrip(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Изтриване на разходка
     * DELETE /api/trips/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }
}
