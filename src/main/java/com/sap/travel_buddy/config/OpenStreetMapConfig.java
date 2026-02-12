package com.sap.travel_buddy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация за OpenStreetMap Nominatim API
 */
@Configuration
@Getter
public class OpenStreetMapConfig {

    @Value("${openstreetmap.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;
    
    @Value("${openstreetmap.user-agent:TravelBuddyApp/1.0}")
    private String userAgent;
}
