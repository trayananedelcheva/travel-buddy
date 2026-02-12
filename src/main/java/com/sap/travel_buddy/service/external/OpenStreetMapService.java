package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.OpenStreetMapConfig;
import com.sap.travel_buddy.domain.Place;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service за интеграция с OpenStreetMap Nominatim API
 * (заменя Google Places API)
 */
@Service
@Slf4j
public class OpenStreetMapService {

    private final WebClient webClient;
    private final OpenStreetMapConfig config;

    public OpenStreetMapService(@Qualifier("openStreetMapWebClient") WebClient webClient,
                                OpenStreetMapConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Търсене на места по текстов query (използва OpenStreetMap Nominatim)
     */
    public List<Place> searchPlacesByText(String query, Double latitude, Double longitude, Integer radius) {
        log.debug("Searching places with query: {} near {},{}", query, latitude, longitude);

        try {
            String uri = UriComponentsBuilder.fromPath("/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 10)
                    .queryParam("addressdetails", 1)
                    .build()
                    .toUriString();

            List results = webClient.get()
                    .uri(uri)
                    .header("User-Agent", config.getUserAgent())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();

            return parseNominatimResponse((List<Map<String, Object>>) results);

        } catch (Exception e) {
            log.error("Error searching places with OpenStreetMap: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Търсене на места наблизо (използва OpenStreetMap Nominatim reverse geocoding + search)
     */
    public List<Place> searchNearbyPlaces(Double latitude, Double longitude, Integer radius, String type) {
        log.debug("Searching nearby places at {},{} with type {}", latitude, longitude, type);

        try {
            String cityQuery = getCityFromCoordinates(latitude, longitude);

            String searchQuery = type != null && !type.isEmpty()
                    ? type + " in " + cityQuery
                    : cityQuery;

            return searchPlacesByText(searchQuery, latitude, longitude, radius);

        } catch (Exception e) {
            log.error("Error searching nearby places: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Взимане на град от координати (reverse geocoding)
     */
    private String getCityFromCoordinates(Double latitude, Double longitude) {
        try {
            String uri = UriComponentsBuilder.fromPath("/reverse")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "json")
                    .build()
                    .toUriString();

            Map<String, Object> result = webClient.get()
                    .uri(uri)
                    .header("User-Agent", config.getUserAgent())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (result != null && result.containsKey("address")) {
                Map<String, Object> address = (Map<String, Object>) result.get("address");
                String city = (String) address.get("city");
                if (city == null) city = (String) address.get("town");
                if (city == null) city = (String) address.get("village");
                if (city != null) return city;
            }

        } catch (Exception e) {
            log.warn("Error getting city from coordinates: {}", e.getMessage());
        }

        return "nearby";
    }

    /**
     * Взимане на детайли за конкретно място (по OSM place_id)
     */
    public Place getPlaceDetails(String placeId) {
        log.debug("Getting details for place: {}", placeId);

        try {
            String uri = UriComponentsBuilder.fromPath("/lookup")
                    .queryParam("osm_ids", placeId)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", 1)
                    .build()
                    .toUriString();

            List results = webClient.get()
                    .uri(uri)
                    .header("User-Agent", config.getUserAgent())
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();

            if (results != null && !results.isEmpty()) {
                return parsePlace((Map<String, Object>) results.get(0));
            }

        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Парсване на OpenStreetMap Nominatim response
     */
    private List<Place> parseNominatimResponse(List<Map<String, Object>> results) {
        List<Place> places = new ArrayList<>();

        if (results == null || results.isEmpty()) {
            log.warn("OpenStreetMap returned no results");
            return places;
        }

        for (Map<String, Object> result : results) {
            Place place = parsePlace(result);
            if (place != null) {
                places.add(place);
            }
        }

        return places;
    }

    /**
     * Парсване на отделно място от OpenStreetMap JSON
     */
    private Place parsePlace(Map<String, Object> json) {
        try {
            Place place = new Place();

            Object placeIdObj = json.get("place_id");
            String placeId = placeIdObj != null ? placeIdObj.toString() : null;
            place.setGooglePlaceId(placeId);

            place.setName((String) json.get("name"));
            place.setAddress((String) json.get("display_name"));

            if (json.containsKey("lat") && json.containsKey("lon")) {
                place.setLatitude(Double.parseDouble(json.get("lat").toString()));
                place.setLongitude(Double.parseDouble(json.get("lon").toString()));
            }

            String osmClass = (String) json.get("class");
            String osmType = (String) json.get("type");
            if (osmClass != null && osmType != null) {
                place.setTypes(osmClass + ":" + osmType);
            }

            place.setRating(null);
            place.setUserRatingsTotal(null);
            place.setCurrentlyOpen(null);
            place.setPhoneNumber(null);
            place.setWebsite(null);

            return place;

        } catch (Exception e) {
            log.error("Error parsing place: {}", e.getMessage(), e);
            return null;
        }
    }
}
