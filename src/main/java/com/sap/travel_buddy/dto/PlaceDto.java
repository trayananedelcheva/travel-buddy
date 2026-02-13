package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO за Place - за изпращане към клиента
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDto {

    private Long id;
    private String googlePlaceId;
    private String name;
    private String address;
    private String formattedAddress;
    private String locality;
    private String region;
    private String country;
    private String postcode;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer userRatingsTotal;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean currentlyOpen;
    private Integer distanceMeters;
    private String timezone;
    private String fsqLink;
    private String types;
    private String categoryIds;
    private String phoneNumber;
    private String website;
    private String photoUrl;
}
