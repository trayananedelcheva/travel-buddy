package com.sap.travel_buddy.mapper;

import com.sap.travel_buddy.domain.WeatherData;
import com.sap.travel_buddy.dto.WeatherDto;
import org.springframework.stereotype.Component;

/**
 * Mapper за конвертиране между WeatherData entity и WeatherDto
 */
@Component
public class WeatherMapper {

    /**
     * Конвертира WeatherData entity към WeatherDto
     */
    public WeatherDto toDto(WeatherData weatherData) {
        if (weatherData == null) {
            return null;
        }

        WeatherDto dto = new WeatherDto();
        dto.setTemperature(weatherData.getTemperature());
        dto.setHumidity(weatherData.getHumidity());
        dto.setWindSpeed(weatherData.getWindSpeed());
        dto.setPrecipitation(weatherData.getPrecipitation());
        dto.setWeatherDescription(weatherData.getWeatherDescription());
        dto.setIsSuitableForTrip(weatherData.getIsSuitableForTrip());
        dto.setForecastTime(weatherData.getForecastTime());

        return dto;
    }

    /**
     * Конвертира WeatherDto към WeatherData entity
     */
    public WeatherData toEntity(WeatherDto dto) {
        if (dto == null) {
            return null;
        }

        WeatherData weatherData = new WeatherData();
        weatherData.setTemperature(dto.getTemperature());
        weatherData.setHumidity(dto.getHumidity());
        weatherData.setWindSpeed(dto.getWindSpeed());
        weatherData.setPrecipitation(dto.getPrecipitation());
        weatherData.setWeatherDescription(dto.getWeatherDescription());
        weatherData.setIsSuitableForTrip(dto.getIsSuitableForTrip());
        weatherData.setForecastTime(dto.getForecastTime());

        return weatherData;
    }
}
