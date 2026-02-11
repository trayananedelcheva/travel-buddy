package com.sap.travel_buddy.mapper;

import com.sap.travel_buddy.domain.Trip;
import com.sap.travel_buddy.dto.TripDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper за конвертиране между Trip entity и TripDto
 */
@Component
@RequiredArgsConstructor
public class TripMapper {

    private final PlaceMapper placeMapper;
    private final WeatherMapper weatherMapper;

    /**
     * Конвертира Trip entity към TripDto
     */
    public TripDto toDto(Trip trip) {
        if (trip == null) {
            return null;
        }

        TripDto dto = new TripDto();
        dto.setId(trip.getId());
        dto.setName(trip.getName());
        dto.setPlannedStartTime(trip.getPlannedStartTime());
        dto.setPlannedEndTime(trip.getPlannedEndTime());
        dto.setStatus(trip.getStatus());
        dto.setRecommendations(trip.getRecommendations());
        dto.setIsRecommended(trip.getIsRecommended());
        dto.setWarningMessage(trip.getWarningMessage());

        // Мапване на places
        if (trip.getPlaces() != null) {
            dto.setPlaces(trip.getPlaces().stream()
                .map(placeMapper::toDto)
                .collect(Collectors.toList()));
        }

        // Мапване на weather
        if (trip.getWeatherData() != null) {
            dto.setWeather(weatherMapper.toDto(trip.getWeatherData()));
        }

        return dto;
    }

    /**
     * Конвертира TripDto към Trip entity (частично - без места и време)
     */
    public Trip toEntity(TripDto dto) {
        if (dto == null) {
            return null;
        }

        Trip trip = new Trip();
        trip.setId(dto.getId());
        trip.setName(dto.getName());
        trip.setPlannedStartTime(dto.getPlannedStartTime());
        trip.setPlannedEndTime(dto.getPlannedEndTime());
        trip.setStatus(dto.getStatus());
        trip.setRecommendations(dto.getRecommendations());
        trip.setIsRecommended(dto.getIsRecommended());
        trip.setWarningMessage(dto.getWarningMessage());

        // Места и време се добавят през service layer

        return trip;
    }
}
