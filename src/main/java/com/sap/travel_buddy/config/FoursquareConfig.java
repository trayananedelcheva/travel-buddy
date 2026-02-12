package com.sap.travel_buddy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация за Foursquare Places API
 */
@Configuration
@Getter
public class FoursquareConfig {

    @Value("${foursquare.api-key}")
    private String apiKey;

    @Value("${foursquare.base-url:https://places-api.foursquare.com}")
    private String baseUrl;
}
