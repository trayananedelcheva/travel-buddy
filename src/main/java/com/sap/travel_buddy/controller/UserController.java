package com.sap.travel_buddy.controller;

import com.sap.travel_buddy.domain.Trip;
import com.sap.travel_buddy.domain.User;
import com.sap.travel_buddy.dto.TripDto;
import com.sap.travel_buddy.dto.UserProfileDto;
import com.sap.travel_buddy.repository.SearchHistoryRepository;
import com.sap.travel_buddy.repository.UserRepository;
import com.sap.travel_buddy.service.TripService;
import com.sap.travel_buddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller за потребителски профил и история
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final TripService tripService;
    private final SearchHistoryRepository searchHistoryRepository;

    /**
     * Взимане на профил на текущия потребител
     * GET /api/users/me
     */
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        User user = SecurityUtil.getCurrentUser();
        
        // Refresh user to get latest data
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfileDto profile = UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .defaultLatitude(user.getDefaultLatitude())
                .defaultLongitude(user.getDefaultLongitude())
                .preferredLanguage(user.getPreferredLanguage())
                .preferredCurrency(user.getPreferredCurrency())
                .createdAt(user.getCreatedAt())
                .tripsCount((long) user.getTrips().size())
                .favoritePlacesCount((long) user.getFavoritePlaces().size())
                .searchHistoryCount((long) searchHistoryRepository.findByUserOrderBySearchedAtDesc(user).size())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(profile);
    }

    /**
     * Взимане на всички разходки на текущия потребител
     * GET /api/users/me/trips
     */
    @GetMapping("/me/trips")
    public ResponseEntity<List<TripDto>> getCurrentUserTrips() {
        List<TripDto> trips = tripService.getCurrentUserTrips();
        return ResponseEntity.ok(trips);
    }

    /**
     * Взимане на разходки на текущия потребител по статус
     * GET /api/users/me/trips/status/{status}
     */
    @GetMapping("/me/trips/status/{status}")
    public ResponseEntity<List<TripDto>> getCurrentUserTripsByStatus(@PathVariable Trip.TripStatus status) {
        List<TripDto> trips = tripService.getCurrentUserTripsByStatus(status);
        return ResponseEntity.ok(trips);
    }

    /**
     * Статистика за текущия потребител
     * GET /api/users/me/stats
     */
    @GetMapping("/me/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCurrentUserStats() {
        User user = SecurityUtil.getCurrentUser();
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        long completedTrips = user.getTrips().stream()
                .filter(trip -> trip.getStatus() == Trip.TripStatus.COMPLETED)
                .count();

        long plannedTrips = user.getTrips().stream()
                .filter(trip -> trip.getStatus() == Trip.TripStatus.PLANNED)
                .count();

        Map<String, Object> stats = Map.of(
                "totalTrips", user.getTrips().size(),
                "completedTrips", completedTrips,
                "plannedTrips", plannedTrips,
                "favoritePlaces", user.getFavoritePlaces().size(),
                "totalSearches", searchHistoryRepository.findByUserOrderBySearchedAtDesc(user).size()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Обновяване на профила на текущия потребител
     * PATCH /api/users/me
     */
    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(@RequestBody Map<String, Object> updates) {
        User user = SecurityUtil.getCurrentUser();
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Обновяване само на позволените полета
        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("defaultLatitude")) {
            user.setDefaultLatitude(((Number) updates.get("defaultLatitude")).doubleValue());
        }
        if (updates.containsKey("defaultLongitude")) {
            user.setDefaultLongitude(((Number) updates.get("defaultLongitude")).doubleValue());
        }
        if (updates.containsKey("preferredLanguage")) {
            user.setPreferredLanguage((String) updates.get("preferredLanguage"));
        }
        if (updates.containsKey("preferredCurrency")) {
            user.setPreferredCurrency((String) updates.get("preferredCurrency"));
        }

        user = userRepository.save(user);
        log.info("User profile updated for: {}", user.getUsername());

        UserProfileDto profile = UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .defaultLatitude(user.getDefaultLatitude())
                .defaultLongitude(user.getDefaultLongitude())
                .preferredLanguage(user.getPreferredLanguage())
                .preferredCurrency(user.getPreferredCurrency())
                .createdAt(user.getCreatedAt())
                .tripsCount((long) user.getTrips().size())
                .favoritePlacesCount((long) user.getFavoritePlaces().size())
                .searchHistoryCount((long) searchHistoryRepository.findByUserOrderBySearchedAtDesc(user).size())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(profile);
    }
}
