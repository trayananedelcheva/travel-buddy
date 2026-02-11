package com.sap.travel_buddy.service;

import com.sap.travel_buddy.domain.Place;
import com.sap.travel_buddy.domain.User;
import com.sap.travel_buddy.dto.PlaceDto;
import com.sap.travel_buddy.mapper.PlaceMapper;
import com.sap.travel_buddy.repository.PlaceRepository;
import com.sap.travel_buddy.repository.UserRepository;
import com.sap.travel_buddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service за управление на любими места
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoritePlaceService {

    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    /**
     * Добавяне на място към любими
     */
    @Transactional
    public void addToFavorites(Long placeId) {
        User user = SecurityUtil.getCurrentUser();
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

        if (!user.getFavoritePlaces().contains(place)) {
            user.getFavoritePlaces().add(place);
            userRepository.save(user);
            log.info("User {} added place {} to favorites", user.getUsername(), placeId);
        }
    }

    /**
     * Премахване на място от любими
     */
    @Transactional
    public void removeFromFavorites(Long placeId) {
        User user = SecurityUtil.getCurrentUser();
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));

        user.getFavoritePlaces().remove(place);
        userRepository.save(user);
        log.info("User {} removed place {} from favorites", user.getUsername(), placeId);
    }

    /**
     * Взимане на всички любими места на потребителя
     */
    public List<PlaceDto> getFavoritePlaces() {
        User user = SecurityUtil.getCurrentUser();
        
        // Refresh user to get latest favorite places
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getFavoritePlaces().stream()
                .map(placeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Проверка дали място е любимо
     */
    public boolean isFavorite(Long placeId) {
        User user = SecurityUtil.getCurrentUser();
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getFavoritePlaces().stream()
                .anyMatch(place -> place.getId().equals(placeId));
    }

    /**
     * Брой любими места
     */
    public long getFavoritesCount() {
        User user = SecurityUtil.getCurrentUser();
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getFavoritePlaces().size();
    }
}
