package com.sap.travel_buddy.service;

import com.sap.travel_buddy.domain.Place;
import com.sap.travel_buddy.dto.PlaceDto;
import com.sap.travel_buddy.dto.PlaceSearchRequest;
import com.sap.travel_buddy.mapper.PlaceMapper;
import com.sap.travel_buddy.repository.PlaceRepository;
import com.sap.travel_buddy.service.external.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic за работа с места
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final GooglePlacesService googlePlacesService;
    private final PlaceMapper placeMapper;

    /**
     * Търсене на места (използва Google Places API)
     */
    @Transactional
    public List<PlaceDto> searchPlaces(PlaceSearchRequest request) {
        log.info("Searching places with request: {}", request);

        List<Place> places;
        
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            // Text search
            places = googlePlacesService.searchPlacesByText(
                request.getQuery(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius()
            );
        } else if (request.getLatitude() != null && request.getLongitude() != null) {
            // Nearby search
            places = googlePlacesService.searchNearbyPlaces(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadius(),
                request.getType()
            );
        } else {
            log.warn("Invalid search request - missing query or location");
            return List.of();
        }

        // Запазване на новите места в базата
        for (Place place : places) {
            saveOrUpdatePlace(place);
        }

        return places.stream()
            .map(placeMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на място по ID
     */
    public Optional<PlaceDto> getPlaceById(Long id) {
        return placeRepository.findById(id)
            .map(placeMapper::toDto);
    }

    /**
     * Взимане на място по Google Place ID
     */
    @Transactional
    public Optional<PlaceDto> getPlaceByGoogleId(String googlePlaceId) {
        Optional<Place> existing = placeRepository.findByGooglePlaceId(googlePlaceId);
        
        if (existing.isPresent()) {
            return existing.map(placeMapper::toDto);
        }

        // Ако няма в базата, взимаме от Google API
        Place place = googlePlacesService.getPlaceDetails(googlePlaceId);
        if (place != null) {
            place = saveOrUpdatePlace(place);
            return Optional.of(placeMapper.toDto(place));
        }

        return Optional.empty();
    }

    /**
     * Търсене на места по име
     */
    public List<PlaceDto> searchPlacesByName(String name) {
        return placeRepository.findByNameContainingIgnoreCase(name).stream()
            .map(placeMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на всички отворени места
     */
    public List<PlaceDto> getOpenPlaces() {
        return placeRepository.findByCurrentlyOpenTrue().stream()
            .map(placeMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на места с минимален рейтинг
     */
    public List<PlaceDto> getPlacesByMinRating(Double minRating) {
        return placeRepository.findByRatingGreaterThanEqual(minRating).stream()
            .map(placeMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Запазване или обновяване на място
     */
    @Transactional
    public Place saveOrUpdatePlace(Place place) {
        Optional<Place> existing = placeRepository.findByGooglePlaceId(place.getGooglePlaceId());
        
        if (existing.isPresent()) {
            // Обновяване на съществуващо място
            Place existingPlace = existing.get();
            updatePlaceData(existingPlace, place);
            return placeRepository.save(existingPlace);
        } else {
            // Създаване на нов запис
            return placeRepository.save(place);
        }
    }

    /**
     * Обновяване на данни на място
     */
    private void updatePlaceData(Place existing, Place newData) {
        existing.setName(newData.getName());
        existing.setAddress(newData.getAddress());
        existing.setLatitude(newData.getLatitude());
        existing.setLongitude(newData.getLongitude());
        existing.setRating(newData.getRating());
        existing.setUserRatingsTotal(newData.getUserRatingsTotal());
        existing.setOpeningTime(newData.getOpeningTime());
        existing.setClosingTime(newData.getClosingTime());
        existing.setCurrentlyOpen(newData.getCurrentlyOpen());
        existing.setTypes(newData.getTypes());
        existing.setPhoneNumber(newData.getPhoneNumber());
        existing.setWebsite(newData.getWebsite());
    }

    /**
     * Проверка дали място е отворено
     */
    public boolean isPlaceOpen(Long placeId) {
        return placeRepository.findById(placeId)
            .map(Place::getCurrentlyOpen)
            .orElse(false);
    }

    /**
     * Изтриване на място
     */
    @Transactional
    public void deletePlace(Long id) {
        placeRepository.deleteById(id);
    }
}
