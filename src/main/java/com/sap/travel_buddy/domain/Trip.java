package com.sap.travel_buddy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity представляваща планирана разходка/пътуване
 */
@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Потребител, който е създал разходката

    @Column(nullable = false)
    private String name; // Име на разходката

    @Column(nullable = false)
    private LocalDateTime plannedStartTime; // Планирано начало

    private LocalDateTime plannedEndTime; // Планиран край

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "trip_places",
        joinColumns = @JoinColumn(name = "trip_id"),
        inverseJoinColumns = @JoinColumn(name = "place_id")
    )
    private List<Place> places = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "weather_data_id")
    private WeatherData weatherData;

    @Enumerated(EnumType.STRING)
    private TripStatus status; // Статус на разходката

    @Column(length = 2000)
    private String recommendations; // Препоръки базирани на данните

    private Boolean isRecommended; // Дали е препоръчително да се тръгне

    @Column(length = 1000)
    private String warningMessage; // Предупреждения (затворено място, лошо време и т.н.)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TripStatus {
        PLANNED,    // Планирана
        CONFIRMED,  // Потвърдена
        CANCELLED,  // Отказана
        COMPLETED   // Завършена
    }
}
