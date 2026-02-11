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
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer userRatingsTotal;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean currentlyOpen;
    private String types;
    private String phoneNumber;
    private String website;
}
