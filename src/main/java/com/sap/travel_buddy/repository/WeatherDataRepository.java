package com.sap.travel_buddy.repository;

import com.sap.travel_buddy.domain.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository за работа с WeatherData entities
 */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Намира последна прогноза за дадена локация
     */
    @Query("SELECT w FROM WeatherData w WHERE " +
           "ABS(w.latitude - :lat) < 0.01 AND ABS(w.longitude - :lon) < 0.01 " +
           "ORDER BY w.fetchedAt DESC")
    List<WeatherData> findRecentWeatherForLocation(
        @Param("lat") Double latitude, 
        @Param("lon") Double longitude
    );

    /**
     * Намира прогноза за конкретно време и локация
     */
    @Query("SELECT w FROM WeatherData w WHERE " +
           "ABS(w.latitude - :lat) < 0.01 AND ABS(w.longitude - :lon) < 0.01 AND " +
           "w.forecastTime BETWEEN :start AND :end " +
           "ORDER BY w.fetchedAt DESC")
    Optional<WeatherData> findWeatherForTimeAndLocation(
        @Param("lat") Double latitude,
        @Param("lon") Double longitude,
        @Param("start") LocalDateTime startTime,
        @Param("end") LocalDateTime endTime
    );

    /**
     * Намира подходящи условия за разходка
     */
    List<WeatherData> findByIsSuitableForTripTrue();

    /**
     * Изтрива стари прогнози (cleanup)
     */
    @Query("DELETE FROM WeatherData w WHERE w.fetchedAt < :cutoffDate")
    void deleteOldForecasts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
