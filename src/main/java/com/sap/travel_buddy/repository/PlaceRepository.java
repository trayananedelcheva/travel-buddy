package com.sap.travel_buddy.repository;

import com.sap.travel_buddy.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository за работа с Place entities
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * Намира място по Google Place ID
     */
    Optional<Place> findByGooglePlaceId(String googlePlaceId);

    /**
     * Намира места по име (частично съвпадение)
     */
    List<Place> findByNameContainingIgnoreCase(String name);

    /**
     * Намира места с рейтинг над определена стойност
     */
    List<Place> findByRatingGreaterThanEqual(Double minRating);

    /**
     * Намира места по тип (частично съвпадение в types string)
     */
    @Query("SELECT p FROM Place p WHERE p.types LIKE %:type%")
    List<Place> findByType(@Param("type") String type);

    /**
     * Намира отворени места
     */
    List<Place> findByCurrentlyOpenTrue();

    /**
     * Проверява дали място с даден Google ID вече съществува
     */
    boolean existsByGooglePlaceId(String googlePlaceId);
}
