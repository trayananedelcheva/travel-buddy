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
        dto.setFormattedAddress(place.getFormattedAddress());
        dto.setLocality(place.getLocality());
        dto.setRegion(place.getRegion());
        dto.setCountry(place.getCountry());
        dto.setPostcode(place.getPostcode());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        dto.setRating(place.getRating());
        dto.setUserRatingsTotal(place.getUserRatingsTotal());
        dto.setOpeningTime(place.getOpeningTime());
        dto.setClosingTime(place.getClosingTime());
        dto.setCurrentlyOpen(place.getCurrentlyOpen());
        dto.setDistanceMeters(place.getDistanceMeters());
        dto.setTimezone(place.getTimezone());
        dto.setFsqLink(place.getFsqLink());
        dto.setTypes(place.getTypes());
        dto.setCategoryIds(place.getCategoryIds());
        dto.setPhoneNumber(place.getPhoneNumber());
        dto.setWebsite(place.getWebsite());
        dto.setPhotoUrl(place.getPhotoUrl());

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
        place.setFormattedAddress(dto.getFormattedAddress());
        place.setLocality(dto.getLocality());
        place.setRegion(dto.getRegion());
        place.setCountry(dto.getCountry());
        place.setPostcode(dto.getPostcode());
        place.setLatitude(dto.getLatitude());
        place.setLongitude(dto.getLongitude());
        place.setRating(dto.getRating());
        place.setUserRatingsTotal(dto.getUserRatingsTotal());
        place.setOpeningTime(dto.getOpeningTime());
        place.setClosingTime(dto.getClosingTime());
        place.setCurrentlyOpen(dto.getCurrentlyOpen());
        place.setDistanceMeters(dto.getDistanceMeters());
        place.setTimezone(dto.getTimezone());
        place.setFsqLink(dto.getFsqLink());
        place.setTypes(dto.getTypes());
        place.setCategoryIds(dto.getCategoryIds());
        place.setPhoneNumber(dto.getPhoneNumber());
        place.setWebsite(dto.getWebsite());
        place.setPhotoUrl(dto.getPhotoUrl());

        return place;
    }
}
