package com.sap.travel_buddy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация за Google Places API properties
 */
@Configuration
@Getter
public class GooglePlacesConfig {

    @Value("${google.places.api-key}")
    private String apiKey;

    @Value("${google.places.base-url}")
    private String baseUrl;
}
