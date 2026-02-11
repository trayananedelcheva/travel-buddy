package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO за създаване на нова разходка
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    private String name;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private List<String> placeSearchQueries; // Търсене по име/адрес
    private Double startLatitude; // Начална локация
    private Double startLongitude;
}
