package com.sap.travel_buddy.repository;

import com.sap.travel_buddy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository за работа с User entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Намиране на потребител по username
     */
    Optional<User> findByUsername(String username);

    /**
     * Намиране на потребител по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверка дали username съществува
     */
    boolean existsByUsername(String username);

    /**
     * Проверка дали email съществува
     */
    boolean existsByEmail(String email);
}
