package com.sap.travel_buddy.controller;

import com.sap.travel_buddy.dto.PlaceDto;
import com.sap.travel_buddy.dto.PlaceSearchRequest;
import com.sap.travel_buddy.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller за работа с места
 */
@RestController
@RequestMapping("/api/places")
@Slf4j
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /**
     * Търсене на места
     * POST /api/places/search
     */
    @PostMapping("/search")
    public ResponseEntity<List<PlaceDto>> searchPlaces(@RequestBody PlaceSearchRequest request) {
        log.info("Search request: {}", request);
        List<PlaceDto> places = placeService.searchPlaces(request);
        return ResponseEntity.ok(places);
    }

    /**
     * Взимане на място по ID
     * GET /api/places/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaceDto> getPlaceById(@PathVariable Long id) {
        return placeService.getPlaceById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Взимане на място по Google Place ID
     * GET /api/places/google/{googlePlaceId}
     */
    @GetMapping("/google/{googlePlaceId}")
    public ResponseEntity<PlaceDto> getPlaceByGoogleId(@PathVariable String googlePlaceId) {
        return placeService.getPlaceByGoogleId(googlePlaceId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Търсене на места по име
     * GET /api/places/search-by-name?name=ресторант
     */
    @GetMapping("/search-by-name")
    public ResponseEntity<List<PlaceDto>> searchByName(@RequestParam String name) {
        List<PlaceDto> places = placeService.searchPlacesByName(name);
        return ResponseEntity.ok(places);
    }

    /**
     * Взимане на всички отворени места
     * GET /api/places/open
     */
    @GetMapping("/open")
    public ResponseEntity<List<PlaceDto>> getOpenPlaces() {
        List<PlaceDto> places = placeService.getOpenPlaces();
        return ResponseEntity.ok(places);
    }

    /**
     * Взимане на места с минимален рейтинг
     * GET /api/places/rated?minRating=4.0
     */
    @GetMapping("/rated")
    public ResponseEntity<List<PlaceDto>> getPlacesByRating(@RequestParam Double minRating) {
        List<PlaceDto> places = placeService.getPlacesByMinRating(minRating);
        return ResponseEntity.ok(places);
    }

    /**
     * Проверка дали място е отворено
     * GET /api/places/{id}/is-open
     */
    @GetMapping("/{id}/is-open")
    public ResponseEntity<Boolean> isPlaceOpen(@PathVariable Long id) {
        boolean isOpen = placeService.isPlaceOpen(id);
        return ResponseEntity.ok(isOpen);
    }

    /**
     * Изтриване на място
     * DELETE /api/places/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        placeService.deletePlace(id);
        return ResponseEntity.noContent().build();
    }
}
