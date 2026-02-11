package com.sap.travel_buddy.mapper;

import com.sap.travel_buddy.domain.Place;
import com.sap.travel_buddy.dto.PlaceDto;
import org.springframework.stereotype.Component;

/**
 * Mapper за конвертиране между Place entity и PlaceDto
 */
@Component
public class PlaceMapper {

    /**
     * Конвертира Place entity към PlaceDto
     */
    public PlaceDto toDto(Place place) {
        if (place == null) {
            return null;
        }

        PlaceDto dto = new PlaceDto();
        dto.setId(place.getId());
        dto.setGooglePlaceId(place.getGooglePlaceId());
        dto.setName(place.getName());
        dto.setAddress(place.getAddress());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        dto.setRating(place.getRating());
        dto.setUserRatingsTotal(place.getUserRatingsTotal());
        dto.setOpeningTime(place.getOpeningTime());
        dto.setClosingTime(place.getClosingTime());
        dto.setCurrentlyOpen(place.getCurrentlyOpen());
        dto.setTypes(place.getTypes());
        dto.setPhoneNumber(place.getPhoneNumber());
        dto.setWebsite(place.getWebsite());

        return dto;
    }

    /**
     * Конвертира PlaceDto към Place entity
     */
    public Place toEntity(PlaceDto dto) {
        if (dto == null) {
            return null;
        }

        Place place = new Place();
        place.setId(dto.getId());
        place.setGooglePlaceId(dto.getGooglePlaceId());
        place.setName(dto.getName());
        place.setAddress(dto.getAddress());
        place.setLatitude(dto.getLatitude());
        place.setLongitude(dto.getLongitude());
        place.setRating(dto.getRating());
        place.setUserRatingsTotal(dto.getUserRatingsTotal());
        place.setOpeningTime(dto.getOpeningTime());
        place.setClosingTime(dto.getClosingTime());
        place.setCurrentlyOpen(dto.getCurrentlyOpen());
        place.setTypes(dto.getTypes());
        place.setPhoneNumber(dto.getPhoneNumber());
        place.setWebsite(dto.getWebsite());

        return place;
    }
}
