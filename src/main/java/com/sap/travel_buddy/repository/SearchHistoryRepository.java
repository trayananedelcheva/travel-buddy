package com.sap.travel_buddy.repository;

import com.sap.travel_buddy.domain.SearchHistory;
import com.sap.travel_buddy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository за работа с SearchHistory entities
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * Намиране на история на търсенията за потребител
     */
    List<SearchHistory> findByUserOrderBySearchedAtDesc(User user);

    /**
     * Намиране на последните N търсения на потребител
     */
    @Query("SELECT s FROM SearchHistory s WHERE s.user = :user ORDER BY s.searchedAt DESC")
    List<SearchHistory> findRecentSearches(@Param("user") User user, org.springframework.data.domain.Pageable pageable);

    /**
     * Намиране на търсения по тип
     */
    List<SearchHistory> findByUserAndSearchTypeOrderBySearchedAtDesc(User user, SearchHistory.SearchType searchType);

    /**
     * Изтриване на стари търсения
     */
    void deleteBySearchedAtBefore(LocalDateTime cutoffDate);

    /**
     * Брой търсения на потребител
     */
    long countByUser(User user);
}
