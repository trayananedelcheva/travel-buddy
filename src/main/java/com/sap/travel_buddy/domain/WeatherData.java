package com.sap.travel_buddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity за съхранение на метеорологични данни от Weather API
 */
@Entity
@Table(name = "weather_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime forecastTime; // За кой момент е прогнозата

    private Double temperature; // В градуси Целзий

    private Integer humidity; // В проценти

    private Double windSpeed; // В km/h

    private Integer precipitation; // Вероятност за валежи (%)

    private String weatherCode; // Код на времето (ясно, облачно, дъжд и т.н.)

    private String weatherDescription; // Описание на времето

    private Boolean isSuitableForTrip; // Изчислена оценка дали е подходящо за разходка

    @Column(nullable = false)
    private LocalDateTime fetchedAt; // Кога е взета прогнозата
}
