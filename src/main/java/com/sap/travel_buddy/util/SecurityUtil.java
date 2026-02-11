package com.sap.travel_buddy.util;

import com.sap.travel_buddy.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility за взимане на текущия authenticated user
 */
@Component
public class SecurityUtil {

    /**
     * Взимане на текущия authenticated user
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        throw new IllegalStateException("Current user is not of type User");
    }

    /**
     * Взимане на username на текущия user
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
