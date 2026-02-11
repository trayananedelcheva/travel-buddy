package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO за "Reality Check" валидацията на разходката
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripValidationResponse {

    private Boolean isRecommended; // Общо препоръчително ли е
    private Integer confidenceScore; // Оценка на сигурност (0-100)
    private WeatherDto weather;
    private List<PlaceValidation> placeValidations = new ArrayList<>();
    private String overallRecommendation;
    private List<String> warnings = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceValidation {
        private String placeName;
        private Boolean isOpen;
        private String openingHoursMessage;
        private Double rating;
        private Boolean isRecommended;
    }
}
