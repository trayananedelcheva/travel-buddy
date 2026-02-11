package com.sap.travel_buddy.repository;

import com.sap.travel_buddy.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository за работа с Trip entities
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Намира разходки по статус
     */
    List<Trip> findByStatus(Trip.TripStatus status);

    /**
     * Намира разходки в определен времеви период
     */
    @Query("SELECT t FROM Trip t WHERE t.plannedStartTime BETWEEN :startDate AND :endDate")
    List<Trip> findTripsInDateRange(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Намира предстоящи разходки (след текущата дата)
     */
    @Query("SELECT t FROM Trip t WHERE t.plannedStartTime > :now ORDER BY t.plannedStartTime ASC")
    List<Trip> findUpcomingTrips(@Param("now") LocalDateTime now);

    /**
     * Намира препоръчани разходки
     */
    List<Trip> findByIsRecommendedTrue();

    /**
     * Намира разходки по име (частично съвпадение)
     */
    List<Trip> findByNameContainingIgnoreCase(String name);

    /**
     * Намира разходки на конкретен потребител
     */
    @Query("SELECT t FROM Trip t WHERE t.user.id = :userId ORDER BY t.plannedStartTime DESC")
    List<Trip> findByUserOrderByPlannedStartTimeDesc(@Param("userId") Long userId);

    /**
     * Намира разходки на конкретен потребител по статус
     */
    @Query("SELECT t FROM Trip t WHERE t.user.id = :userId AND t.status = :status ORDER BY t.plannedStartTime DESC")
    List<Trip> findByUserAndStatus(@Param("userId") Long userId, @Param("status") Trip.TripStatus status);

    /**
     * Брой разходки по статус
     */
    long countByStatus(Trip.TripStatus status);
}
