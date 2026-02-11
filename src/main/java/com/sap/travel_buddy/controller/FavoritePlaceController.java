package com.sap.travel_buddy.controller;

import com.sap.travel_buddy.dto.PlaceDto;
import com.sap.travel_buddy.service.FavoritePlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller за управление на любими места
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    /**
     * Добавяне на място към любими
     * POST /api/favorites/{placeId}
     */
    @PostMapping("/{placeId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long placeId) {
        log.info("Adding place {} to favorites", placeId);
        favoritePlaceService.addToFavorites(placeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Премахване на място от любими
     * DELETE /api/favorites/{placeId}
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long placeId) {
        log.info("Removing place {} from favorites", placeId);
        favoritePlaceService.removeFromFavorites(placeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Взимане на всички любими места
     * GET /api/favorites
     */
    @GetMapping
    public ResponseEntity<List<PlaceDto>> getFavoritePlaces() {
        List<PlaceDto> favorites = favoritePlaceService.getFavoritePlaces();
        return ResponseEntity.ok(favorites);
    }

    /**
     * Проверка дали място е любимо
     * GET /api/favorites/{placeId}/check
     */
    @GetMapping("/{placeId}/check")
    public ResponseEntity<Map<String, Boolean>> isFavorite(@PathVariable Long placeId) {
        boolean isFavorite = favoritePlaceService.isFavorite(placeId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    /**
     * Брой любими места
     * GET /api/favorites/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFavoritesCount() {
        long count = favoritePlaceService.getFavoritesCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
