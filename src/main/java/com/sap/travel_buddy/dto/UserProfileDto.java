package com.sap.travel_buddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO за потребителски профил
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Double defaultLatitude;
    private Double defaultLongitude;
    private String preferredLanguage;
    private String preferredCurrency;
    private LocalDateTime createdAt;
    private Long tripsCount;
    private Long favoritePlacesCount;
    private Long searchHistoryCount;
    private String role;
}
