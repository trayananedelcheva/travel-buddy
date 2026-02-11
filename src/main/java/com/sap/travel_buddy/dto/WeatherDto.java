package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO за WeatherData - за изпращане към клиента
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDto {

    private Double temperature;
    private Integer humidity;
    private Double windSpeed;
    private Integer precipitation;
    private String weatherDescription;
    private Boolean isSuitableForTrip;
    private LocalDateTime forecastTime;
}
