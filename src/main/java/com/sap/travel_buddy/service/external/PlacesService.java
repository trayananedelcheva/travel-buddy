package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.FoursquareConfig;
import com.sap.travel_buddy.domain.Place;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service za integraciq s Foursquare Places API v3
 * (zamenia Google Places API)
 */
@Service
@Slf4j
public class PlacesService {

    private final WebClient webClient;
    private final FoursquareConfig config;

    public PlacesService(@Qualifier("foursquareWebClient") WebClient webClient, 
                               FoursquareConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Търсене на места по текстов query (Foursquare Places API v3)
     */
    public List<Place> searchPlacesByText(String query, Double latitude, Double longitude, Integer radius) {
        log.debug("Searching places with query: {} near {},{}", query, latitude, longitude);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/places/search")
                    .queryParam("query", query)
                    .queryParam("limit", 20);
            
            // Ако има координати, добави геолокация за по-добри резултати
            if (latitude != null && longitude != null) {
                builder.queryParam("ll", latitude + "," + longitude);
                if (radius != null) {
                    builder.queryParam("radius", radius);
                }
            }

            String uri = builder.build().toUriString();

            Map<String, Object> response = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
.header("X-Places-Api-Version", "2025-06-17")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseFoursquareResponse(response);
            
        } catch (Exception e) {
            log.error("Error searching places with Foursquare: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Търсене на места наблизо (Foursquare Places API nearby)
     */
    public List<Place> searchNearbyPlaces(Double latitude, Double longitude, Integer radius, String type) {
        log.debug("Searching nearby places at {},{} with type {}", latitude, longitude, type);

        try {
            if (latitude == null || longitude == null) {
                log.warn("Cannot search nearby without coordinates");
                return new ArrayList<>();
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/places/search")
                    .queryParam("ll", latitude + "," + longitude)
                    .queryParam("radius", radius != null ? radius : 5000)
                    .queryParam("limit", 20);
            
            // Foursquare category (museum, restaurant, etc.)
            if (type != null && !type.isEmpty()) {
                builder.queryParam("query", type);
            }

            String uri = builder.build().toUriString();

            Map<String, Object> response = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("X-Places-Api-Version", "2025-06-17")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseFoursquareResponse(response);
            
        } catch (Exception e) {
            log.error("Error searching nearby places: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Взимане на детайли за конкретно място (по Foursquare fsq_id)
     */
    public Place getPlaceDetails(String placeId) {
        log.debug("Getting details for place: {}", placeId);

        try {
            String uri = "/places/" + placeId;

            Map<String, Object> response = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("X-Places-Api-Version", "2025-06-17")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                return parsePlace(response);
            }
            
        } catch (Exception e) {
            log.error("Error getting place details: {}", e.getMessage(), e);
        }
        
        return null;
    }

    /**
     * Взимане на снимки за място (Foursquare Photos)
     */
    public List<String> getPlacePhotos(String placeId) {
        log.debug("Getting photos for place: {}", placeId);
        List<String> photoUrls = new ArrayList<>();

        try {
            String uri = "/places/" + placeId + "/photos?limit=5";

            List photos = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("X-Places-Api-Version", "2025-06-17")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .block();

            if (photos != null) {
                for (Object photoObj : photos) {
                    Map<String, Object> photo = (Map<String, Object>) photoObj;
                    String prefix = (String) photo.get("prefix");
                    String suffix = (String) photo.get("suffix");
                    if (prefix != null && suffix != null) {
                        // Foursquare image format: {prefix}{size}{suffix}
                        // Size: original, 300x300, etc.
                        String photoUrl = prefix + "original" + suffix;
                        photoUrls.add(photoUrl);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error getting place photos: {}", e.getMessage(), e);
        }
        
        return photoUrls;
    }

    /**
     * Парсване на Foursquare Places API response
     */
    private List<Place> parseFoursquareResponse(Map<String, Object> response) {
        List<Place> places = new ArrayList<>();
        
        if (response == null || !response.containsKey("results")) {
            log.warn("Foursquare API returned no results");
            return places;
        }

        List results = (List) response.get("results");
        if (results != null) {
            for (Object resultObj : results) {
                Map<String, Object> result = (Map<String, Object>) resultObj;
                Place place = parsePlace(result);
                if (place != null) {
                    places.add(place);
                    
                    // Зарежда снимки асинхронно (не блокира)
                    String fsqId = (String) result.get("fsq_id");
                    if (fsqId != null) {
                        try {
                            List<String> photos = getPlacePhotos(fsqId);
                            if (!photos.isEmpty()) {
                                place.setWebsite(photos.get(0)); // Временно в website field
                            }
                        } catch (Exception e) {
                            log.debug("Could not load photos for place: {}", fsqId);
                        }
                    }
                }
            }
        }

        return places;
    }

    /**
     * Парсване на отделно място от Foursquare JSON
     */
    private Place parsePlace(Map<String, Object> json) {
        try {
            Place place = new Place();
            
            // Foursquare fsq_id (unique identifier)
            String fsqId = (String) json.get("fsq_id");
            place.setGooglePlaceId(fsqId); // Използваме същото поле
            
            place.setName((String) json.get("name"));

            // Location (address + coordinates)
            Map<String, Object> location = (Map<String, Object>) json.get("location");
            if (location != null) {
                // Address
                String address = (String) location.get("formatted_address");
                if (address == null) {
                    address = (String) location.get("address");
                }
                place.setAddress(address);

                // Coordinates
                Map<String, Object> geocodes = (Map<String, Object>) json.get("geocodes");
                if (geocodes != null) {
                    Map<String, Object> main = (Map<String, Object>) geocodes.get("main");
                    if (main != null) {
                        Object lat = main.get("latitude");
                        Object lng = main.get("longitude");
                        if (lat != null) place.setLatitude(((Number) lat).doubleValue());
                        if (lng != null) place.setLongitude(((Number) lng).doubleValue());
                    }
                }
            }

            // Rating (0.0 - 10.0 in Foursquare, convert to 5.0 scale)
            if (json.containsKey("rating")) {
                Object ratingObj = json.get("rating");
                if (ratingObj != null) {
                    double fsqRating = ((Number) ratingObj).doubleValue();
                    place.setRating(fsqRating / 2.0); // Convert 10-scale to 5-scale
                }
            }

            // Stats (ratings count)
            Map<String, Object> stats = (Map<String, Object>) json.get("stats");
            if (stats != null && stats.containsKey("total_ratings")) {
                place.setUserRatingsTotal(((Number) stats.get("total_ratings")).intValue());
            }

            // Categories (types)
            List categories = (List) json.get("categories");
            if (categories != null && !categories.isEmpty()) {
                List<String> categoryNames = new ArrayList<>();
                for (Object catObj : categories) {
                    Map<String, Object> cat = (Map<String, Object>) catObj;
                    String catName = (String) cat.get("name");
                    if (catName != null) categoryNames.add(catName);
                }
                place.setTypes(String.join(",", categoryNames));
            }

            // Opening hours
            Map<String, Object> hours = (Map<String, Object>) json.get("hours");
            if (hours != null) {
                List<Map<String, Object>> regularHours = (List<Map<String, Object>>) hours.get("regular");
                if (regularHours != null && !regularHours.isEmpty()) {
                    // Simplified: check if there are any hours defined
                    place.setCurrentlyOpen(true); // Assume open if hours exist
                }
            }

            // Contact
            if (json.containsKey("tel")) {
                place.setPhoneNumber((String) json.get("tel"));
            }
            if (json.containsKey("website")) {
                place.setWebsite((String) json.get("website"));
            }

            return place;
            
        } catch (Exception e) {
            log.error("Error parsing place: {}", e.getMessage(), e);
            return null;
        }
    }
}
