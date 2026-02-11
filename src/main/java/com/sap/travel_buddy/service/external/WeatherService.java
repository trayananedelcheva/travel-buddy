package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.WeatherConfig;
import com.sap.travel_buddy.domain.WeatherData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service за интеграция с Weather API (Open-Meteo)
 */
@Service
@Slf4j
public class WeatherService {

    private final WebClient webClient;
    private final WeatherConfig config;

    public WeatherService(@Qualifier("weatherWebClient") WebClient webClient, 
                          WeatherConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Взимане на прогноза за конкретна локация и време
     */
    public WeatherData getForecast(Double latitude, Double longitude, LocalDateTime forecastTime) {
        log.debug("Getting weather forecast for {},{} at {}", latitude, longitude, forecastTime);

        try {
            // Open-Meteo API endpoint
            String url = String.format("/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m,weather_code&timezone=auto",
                    latitude, longitude);

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseWeatherResponse(response, latitude, longitude, forecastTime);
            
        } catch (Exception e) {
            log.error("Error getting weather forecast: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Взимане на текуща прогноза за локация
     */
    public WeatherData getCurrentWeather(Double latitude, Double longitude) {
        return getForecast(latitude, longitude, LocalDateTime.now());
    }

    /**
     * Парсване на Open-Meteo response
     */
    private WeatherData parseWeatherResponse(Map<String, Object> response, 
                                            Double latitude, 
                                            Double longitude, 
                                            LocalDateTime targetTime) {
        try {
            Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
            if (hourly == null) {
                return null;
            }

            List<String> times = (List<String>) hourly.get("time");
            List<Number> temperatures = (List<Number>) hourly.get("temperature_2m");
            List<Number> humidities = (List<Number>) hourly.get("relative_humidity_2m");
            List<Number> precipitations = (List<Number>) hourly.get("precipitation_probability");
            List<Number> windSpeeds = (List<Number>) hourly.get("wind_speed_10m");
            List<Number> weatherCodes = (List<Number>) hourly.get("weather_code");

            // Намираме най-близкия час до targetTime
            int closestIndex = findClosestTimeIndex(times, targetTime);
            if (closestIndex == -1) {
                return null;
            }

            WeatherData weatherData = new WeatherData();
            weatherData.setLatitude(latitude);
            weatherData.setLongitude(longitude);
            weatherData.setForecastTime(LocalDateTime.parse(times.get(closestIndex), 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            weatherData.setTemperature(temperatures.get(closestIndex).doubleValue());
            weatherData.setHumidity(humidities.get(closestIndex).intValue());
            weatherData.setPrecipitation(precipitations.get(closestIndex).intValue());
            weatherData.setWindSpeed(windSpeeds.get(closestIndex).doubleValue());
            
            int weatherCode = weatherCodes.get(closestIndex).intValue();
            weatherData.setWeatherCode(String.valueOf(weatherCode));
            weatherData.setWeatherDescription(getWeatherDescription(weatherCode));
            
            // Оценка дали е подходящо за разходка
            weatherData.setIsSuitableForTrip(evaluateWeatherSuitability(weatherData));
            
            weatherData.setFetchedAt(LocalDateTime.now());

            return weatherData;
            
        } catch (Exception e) {
            log.error("Error parsing weather response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Намиране на най-близкия час в прогнозата
     */
    private int findClosestTimeIndex(List<String> times, LocalDateTime targetTime) {
        int closestIndex = -1;
        long minDifference = Long.MAX_VALUE;

        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            long difference = Math.abs(java.time.Duration.between(time, targetTime).toMinutes());
            
            if (difference < minDifference) {
                minDifference = difference;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Оценка дали времето е подходящо за разходка
     */
    private Boolean evaluateWeatherSuitability(WeatherData weather) {
        // Критерии:
        // - Температура между 5 и 35 градуса
        // - Вероятност за валежи под 30%
        // - Вятър под 30 km/h
        
        if (weather.getTemperature() < 5 || weather.getTemperature() > 35) {
            return false;
        }
        
        if (weather.getPrecipitation() > 30) {
            return false;
        }
        
        return weather.getWindSpeed() <= 30;
    }

    /**
     * Мапване на WMO Weather Code към описание
     * https://open-meteo.com/en/docs
     */
    private String getWeatherDescription(int code) {
        if (code == 0) return "Ясно небе";
        if (code == 1) return "Предимно ясно";
        if (code == 2) return "Частично облачно";
        if (code == 3) return "Облачно";
        if (code == 45 || code == 48) return "Мъгла";
        if (code == 51 || code == 53 || code == 55) return "Ръмеж";
        if (code == 61 || code == 63 || code == 65) return "Дъжд";
        if (code == 71 || code == 73 || code == 75) return "Сняг";
        if (code == 77) return "Снежинки";
        if (code == 80 || code == 81 || code == 82) return "Краткотрайни валежи";
        if (code == 85 || code == 86) return "Снежни превалявания";
        if (code == 95) return "Гръмотевична буря";
        if (code == 96 || code == 99) return "Гръмотевична буря с градушка";
        return "Неизвестно";
    }
}
