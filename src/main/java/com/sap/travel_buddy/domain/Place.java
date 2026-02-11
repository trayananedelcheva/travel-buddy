package com.sap.travel_buddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Entity представляваща място (Point of Interest) от Google Places API
 */
@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String googlePlaceId; // ID от Google Places API

    @Column(nullable = false)
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private Double rating; // Рейтинг от Google (0-5)

    private Integer userRatingsTotal; // Брой потребителски рейтинги

    // Работно време (опростено - можем да разширим с повече дни)
    private LocalTime openingTime;
    private LocalTime closingTime;

    private Boolean currentlyOpen; // Дали е отворено в момента

    @Column(length = 1000)
    private String types; // Типове на мястото (ресторант, музей и т.н.), съхранени като comma-separated

    private String phoneNumber;

    private String website;
}
