package com.sap.travel_buddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурация за WebClient за external API calls
 */
@Configuration
public class WebClientConfig {

    @Value("${api.timeout.connection:10}")
    private int connectionTimeout;

    @Value("${api.timeout.read:30}")
    private int readTimeout;

    /**
     * WebClient за Google Places API
     */
    @Bean(name = "googlePlacesWebClient")
    public WebClient googlePlacesWebClient(@Value("${google.places.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * WebClient за Weather API
     */
    @Bean(name = "weatherWebClient")
    public WebClient weatherWebClient(@Value("${weather.api.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Общ WebClient за други нужди
     */
    @Bean(name = "defaultWebClient")
    public WebClient defaultWebClient() {
        return WebClient.builder().build();
    }
}
