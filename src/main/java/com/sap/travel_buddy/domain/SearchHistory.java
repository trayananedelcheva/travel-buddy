package com.sap.travel_buddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SearchHistory entity - история на търсенията на потребител
 */
@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchType searchType;

    @Column(nullable = false, length = 500)
    private String searchQuery; // Текст на търсенето

    private Double latitude; // Локация на търсенето
    private Double longitude;

    private Integer radius;

    private String placeType; // Тип място (restaurant, museum и т.н.)

    private Integer resultsCount; // Брой намерени резултати

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    protected void onCreate() {
        searchedAt = LocalDateTime.now();
    }

    public enum SearchType {
        PLACE_TEXT_SEARCH,      // Търсене по текст
        PLACE_NEARBY_SEARCH,    // Търсене наблизо
        TRIP_CREATION          // Създаване на разходка
    }
}
