package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.GooglePlacesConfig;
import com.sap.travel_buddy.domain.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service за интеграция с Google Places API
 */
@Service
@Slf4j
public class GooglePlacesService {

    private final WebClient webClient;
    private final GooglePlacesConfig config;

    public GooglePlacesService(@Qualifier("googlePlacesWebClient") WebClient webClient, 
                               GooglePlacesConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Търсене на места по текстов query
     */
    public List<Place> searchPlacesByText(String query, Double latitude, Double longitude, Integer radius) {
        log.debug("Searching places with query: {}", query);

        try {
            String url = String.format("/textsearch/json?query=%s&key=%s", 
                    query, config.getApiKey());
            
            if (latitude != null && longitude != null) {
                url += String.format("&location=%f,%f", latitude, longitude);
            }
            if (radius != null) {
                url += String.format("&radius=%d", radius);
            }

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseGooglePlacesResponse(response);
            
        } catch (Exception e) {
            log.error("Error searching places: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Търсене на места наблизо
     */
    public List<Place> searchNearbyPlaces(Double latitude, Double longitude, Integer radius, String type) {
        log.debug("Searching nearby places at {},{} with radius {}", latitude, longitude, radius);

        try {
            String url = String.format("/nearbysearch/json?location=%f,%f&radius=%d&key=%s",
                    latitude, longitude, radius != null ? radius : 5000, config.getApiKey());
            
            if (type != null && !type.isEmpty()) {
                url += String.format("&type=%s", type);
            }

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseGooglePlacesResponse(response);
            
        } catch (Exception e) {
            log.error("Error searching nearby places: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Взимане на детайли за конкретно място
     */
    public Place getPlaceDetails(String placeId) {
        log.debug("Getting details for place: {}", placeId);

        try {
            String url = String.format("/details/json?place_id=%s&key=%s&fields=name,formatted_address,geometry,rating,user_ratings_total,opening_hours,types,formatted_phone_number,website",
                    placeId, config.getApiKey());

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "OK".equals(response.get("status"))) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                return parsePlace(result, placeId);
            }
            
        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Парсване на Google Places API response
     */
    private List<Place> parseGooglePlacesResponse(Map<String, Object> response) {
        List<Place> places = new ArrayList<>();
        
        if (response == null || !"OK".equals(response.get("status"))) {
            log.warn("Google Places API returned non-OK status: {}", 
                    response != null ? response.get("status") : "null");
            return places;
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results != null) {
            for (Map<String, Object> result : results) {
                String placeId = (String) result.get("place_id");
                Place place = parsePlace(result, placeId);
                if (place != null) {
                    places.add(place);
                }
            }
        }

        return places;
    }

    /**
     * Парсване на отделно място от JSON
     */
    private Place parsePlace(Map<String, Object> json, String placeId) {
        try {
            Place place = new Place();
            place.setGooglePlaceId(placeId);
            place.setName((String) json.get("name"));
            place.setAddress((String) json.get("formatted_address"));

            // Geometry (координати)
            Map<String, Object> geometry = (Map<String, Object>) json.get("geometry");
            if (geometry != null) {
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                if (location != null) {
                    place.setLatitude(((Number) location.get("lat")).doubleValue());
                    place.setLongitude(((Number) location.get("lng")).doubleValue());
                }
            }

            // Рейтинг
            if (json.containsKey("rating")) {
                place.setRating(((Number) json.get("rating")).doubleValue());
            }
            if (json.containsKey("user_ratings_total")) {
                place.setUserRatingsTotal(((Number) json.get("user_ratings_total")).intValue());
            }

            // Типове
            List<String> types = (List<String>) json.get("types");
            if (types != null && !types.isEmpty()) {
                place.setTypes(String.join(",", types));
            }

            // Работно време (опростено)
            Map<String, Object> openingHours = (Map<String, Object>) json.get("opening_hours");
            if (openingHours != null) {
                place.setCurrentlyOpen((Boolean) openingHours.get("open_now"));
                // TODO: парсване на конкретни часове
            }

            // Контакти
            place.setPhoneNumber((String) json.get("formatted_phone_number"));
            place.setWebsite((String) json.get("website"));

            return place;
            
        } catch (Exception e) {
            log.error("Error parsing place: {}", e.getMessage());
            return null;
        }
    }
}
