package com.sap.travel_buddy.dto;

import com.sap.travel_buddy.domain.Trip;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO за Trip - за изпращане към клиента
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripDto {

    private Long id;
    private String name;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private List<PlaceDto> places;
    private WeatherDto weather;
    private Trip.TripStatus status;
    private String recommendations;
    private Boolean isRecommended;
    private String warningMessage;
}
