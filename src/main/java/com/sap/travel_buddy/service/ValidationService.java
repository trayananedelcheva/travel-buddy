package com.sap.travel_buddy.service;

import com.sap.travel_buddy.domain.Place;
import com.sap.travel_buddy.domain.Trip;
import com.sap.travel_buddy.domain.WeatherData;
import com.sap.travel_buddy.dto.TripValidationResponse;
import com.sap.travel_buddy.dto.WeatherDto;
import com.sap.travel_buddy.mapper.WeatherMapper;
import com.sap.travel_buddy.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service за "Reality Check" валидация на разходки
 * Проверява условия за разходка: време, места, прогноза
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final TripRepository tripRepository;
    private final WeatherMapper weatherMapper;

    /**
     * Извършва пълна Reality Check валидация на разходка
     */
    @Transactional
    public TripValidationResponse validateTrip(Long tripId) {
        log.info("Performing reality check for trip: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        TripValidationResponse response = new TripValidationResponse();
        List<String> warnings = new ArrayList<>();
        int confidenceScore = 100;

        // 1. Проверка на прогнозата
        WeatherData weather = trip.getWeatherData();
        if (weather != null) {
            WeatherDto weatherDto = weatherMapper.toDto(weather);
            response.setWeather(weatherDto);

            if (Boolean.FALSE.equals(weather.getIsSuitableForTrip())) {
                warnings.add("⚠️ Лошо време: " + weather.getWeatherDescription());
                confidenceScore -= 30;
                
                if (weather.getPrecipitation() > 50) {
                    warnings.add("🌧️ Висока вероятност за дъжд (" + weather.getPrecipitation() + "%)");
                }
                if (weather.getTemperature() < 5) {
                    warnings.add("❄️ Много студено (" + weather.getTemperature() + "°C)");
                } else if (weather.getTemperature() > 35) {
                    warnings.add("🔥 Много горещо (" + weather.getTemperature() + "°C)");
                }
                if (weather.getWindSpeed() > 30) {
                    warnings.add("💨 Силен вятър (" + weather.getWindSpeed() + " km/h)");
                }
            }
        } else {
            warnings.add("⚠️ Няма данни за времето");
            confidenceScore -= 20;
        }

        // 2. Проверка на местата
        List<TripValidationResponse.PlaceValidation> placeValidations = new ArrayList<>();
        
        if (trip.getPlaces() == null || trip.getPlaces().isEmpty()) {
            warnings.add("⚠️ Няма добавени места за посещение");
            confidenceScore -= 40;
        } else {
            for (Place place : trip.getPlaces()) {
                TripValidationResponse.PlaceValidation validation = validatePlace(place);
                placeValidations.add(validation);
                
                if (Boolean.FALSE.equals(validation.getIsRecommended())) {
                    confidenceScore -= 15;
                }
            }
        }
        response.setPlaceValidations(placeValidations);

        // 3. Проверка на времето на разходката
        LocalDateTime now = LocalDateTime.now();
        if (trip.getPlannedStartTime().isBefore(now)) {
            warnings.add("⏰ Планираното начало е в миналото");
            confidenceScore -= 50;
        } else if (trip.getPlannedStartTime().isBefore(now.plusHours(2))) {
            warnings.add("⏰ Започва скоро - имате малко време за подготовка");
        }

        // 4. Изчисляване на финална препоръка
        response.setConfidenceScore(Math.max(0, confidenceScore));
        response.setIsRecommended(confidenceScore >= 50);
        response.setWarnings(warnings);

        // 5. Генериране на обща препоръка
        String overallRecommendation = generateOverallRecommendation(response);
        response.setOverallRecommendation(overallRecommendation);

        // 6. Обновяване на Trip entity с резултатите
        updateTripWithValidation(trip, response);

        log.info("Reality check completed for trip {}: confidence {}%, recommended: {}", 
                 tripId, response.getConfidenceScore(), response.getIsRecommended());

        return response;
    }

    /**
     * Валидация на отделно място
     */
    private TripValidationResponse.PlaceValidation validatePlace(Place place) {
        TripValidationResponse.PlaceValidation validation = new TripValidationResponse.PlaceValidation();
        validation.setPlaceName(place.getName());
        validation.setRating(place.getRating());

        // Проверка дали е отворено
        Boolean isOpen = place.getCurrentlyOpen();
        validation.setIsOpen(Boolean.TRUE.equals(isOpen));

        // Генериране на съобщение за работно време
        String openingHoursMessage = generateOpeningHoursMessage(place);
        validation.setOpeningHoursMessage(openingHoursMessage);

        // Оценка дали е препоръчително
        boolean isRecommended = true;
        
        if (Boolean.FALSE.equals(isOpen)) {
            isRecommended = false;
        }
        
        if (place.getRating() != null && place.getRating() < 3.0) {
            isRecommended = false;
        }

        validation.setIsRecommended(isRecommended);

        return validation;
    }

    /**
     * Генериране на съобщение за работно време
     */
    private String generateOpeningHoursMessage(Place place) {
        if (Boolean.TRUE.equals(place.getCurrentlyOpen())) {
            if (place.getOpeningTime() != null && place.getClosingTime() != null) {
                return String.format("✅ Отворено (%s - %s)", 
                    place.getOpeningTime(), place.getClosingTime());
            }
            return "✅ Отворено";
        } else if (Boolean.FALSE.equals(place.getCurrentlyOpen())) {
            if (place.getOpeningTime() != null) {
                return String.format("❌ Затворено (отваря в %s)", place.getOpeningTime());
            }
            return "❌ Затворено";
        } else {
            return "❓ Няма информация за работно време";
        }
    }

    /**
     * Генериране на обща препоръка
     */
    private String generateOverallRecommendation(TripValidationResponse response) {
        int score = response.getConfidenceScore();

        if (score >= 80) {
            return "🎉 Отлични условия за разходка! Всичко изглежда перфектно.";
        } else if (score >= 60) {
            return "👍 Добри условия за разходка, има само няколко незначителни проблема.";
        } else if (score >= 40) {
            return "⚠️ Средни условия - препоръчваме да обърнете внимание на предупрежденията.";
        } else if (score >= 20) {
            return "❌ Лоши условия - не е препоръчително да тръгвате сега.";
        } else {
            return "🚫 Много лоши условия - силно не препоръчваме да тръгвате!";
        }
    }

    /**
     * Обновяване на Trip entity с резултатите от валидацията
     */
    @Transactional
    private void updateTripWithValidation(Trip trip, TripValidationResponse response) {
        trip.setIsRecommended(response.getIsRecommended());
        trip.setRecommendations(response.getOverallRecommendation());
        
        if (!response.getWarnings().isEmpty()) {
            trip.setWarningMessage(String.join("\n", response.getWarnings()));
        }

        tripRepository.save(trip);
    }

    /**
     * Бърза проверка дали разходката е препоръчителна
     */
    public boolean isRecommended(Long tripId) {
        TripValidationResponse validation = validateTrip(tripId);
        return validation.getIsRecommended();
    }
}
