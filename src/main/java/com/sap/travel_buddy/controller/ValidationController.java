package com.sap.travel_buddy.controller;

import com.sap.travel_buddy.dto.TripValidationResponse;
import com.sap.travel_buddy.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller за "Reality Check" валидация на разходки
 */
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@Slf4j
public class ValidationController {

    private final ValidationService validationService;

    /**
     * Извършва пълна Reality Check валидация на разходка
     * POST /api/validation/trips/{tripId}
     * 
     * Проверява:
     * - Прогнозата за времето
     * - Дали местата са отворени
     * - Рейтинги на местата
     * - Времето на разходката
     * 
     * Връща confidence score и препоръки
     */
    @PostMapping("/trips/{tripId}")
    public ResponseEntity<TripValidationResponse> validateTrip(@PathVariable Long tripId) {
        log.info("Reality check requested for trip: {}", tripId);
        
        try {
            TripValidationResponse validation = validationService.validateTrip(tripId);
            return ResponseEntity.ok(validation);
        } catch (IllegalArgumentException e) {
            log.error("Trip not found: {}", tripId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Бърза проверка дали разходката е препоръчителна
     * GET /api/validation/trips/{tripId}/is-recommended
     */
    @GetMapping("/trips/{tripId}/is-recommended")
    public ResponseEntity<Boolean> isRecommended(@PathVariable Long tripId) {
        try {
            boolean recommended = validationService.isRecommended(tripId);
            return ResponseEntity.ok(recommended);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
