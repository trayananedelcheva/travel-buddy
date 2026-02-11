package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO за търсене на места през Google Places API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchRequest {

    private String query; // Търсене по текст
    private Double latitude; // Координати за nearby търсене
    private Double longitude;
    private Integer radius; // Радиус в метри (default: 5000)
    private String type; // Тип място (restaurant, museum и т.н.)
}
