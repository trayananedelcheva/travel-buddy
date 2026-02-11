package com.sap.travel_buddy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация за Weather API properties
 */
@Configuration
@Getter
public class WeatherConfig {

    @Value("${weather.api.base-url}")
    private String baseUrl;
}
