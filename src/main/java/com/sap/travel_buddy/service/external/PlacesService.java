package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.FoursquareConfig;
import com.sap.travel_buddy.domain.Place;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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

    private static final String API_VERSION = "2025-06-17";
        private static final String SEARCH_PATH = "/places/search";

    private final WebClient webClient;
    private final FoursquareConfig config;

    public PlacesService(@Qualifier("foursquareWebClient") WebClient webClient,
                         FoursquareConfig config) {
        this.webClient = webClient;
        this.config = config;
    }

    /**
     * Tursene na mesta po tekstov query (Foursquare Places API v3)
     */
    public List<Place> searchPlacesByText(String query, Double latitude, Double longitude, Integer radius) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        String uri = buildSearchUri(query, latitude, longitude, radius);
        Map<String, Object> response = getForMap(uri);
        return parseFoursquareResponse(response);
    }

    /**
     * Tursene na mesta nablizo (Foursquare Places API nearby)
     */
    public List<Place> searchNearbyPlaces(Double latitude, Double longitude, Integer radius, String type) {
        if (latitude == null || longitude == null) {
            log.warn("Cannot search nearby without coordinates");
            return new ArrayList<>();
        }

        String uri = buildSearchUri(type, latitude, longitude, radius);
        Map<String, Object> response = getForMap(uri);
        return parseFoursquareResponse(response);
    }

    /**
     * Vzimane na detayli za konkretno myasto (po Foursquare fsq_place_id)
     */
    public Place getPlaceDetails(String placeId) {
        if (placeId == null || placeId.isBlank()) {
            return null;
        }

        String uri = UriComponentsBuilder.fromUriString(resolveBaseUrl())
            .path("/places/" + placeId)
            .build()
            .toUriString();

        Map<String, Object> response = getForMap(uri);
        return response != null ? parsePlace(response) : null;
    }

    /**
     * Vzimane na snimki za myasto (Foursquare Photos)
     */
    public List<String> getPlacePhotos(String placeId) {
        List<String> photoUrls = new ArrayList<>();
        if (placeId == null || placeId.isBlank()) {
            return photoUrls;
        }

        String uri = UriComponentsBuilder.fromUriString(resolveBaseUrl())
                .path("/places/" + placeId + "/photos")
                .queryParam("limit", 5)
                .build()
                .toUriString();

        try {
            List photos = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("X-Places-Api-Version", API_VERSION)
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
                        String photoUrl = prefix + "original" + suffix;
                        photoUrls.add(photoUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error getting place photos: {}", e.getMessage());
        }

        return photoUrls;
    }

    private String buildSearchUri(String query, Double latitude, Double longitude, Integer radius) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resolveBaseUrl())
            .path(SEARCH_PATH)
            .queryParam("limit", 20);

        if (query != null && !query.isBlank()) {
            builder.queryParam("query", query);
        }

        if (latitude != null && longitude != null) {
            builder.queryParam("ll", latitude + "," + longitude);
            if (radius != null) {
                builder.queryParam("radius", radius);
            }
        }

        return builder.build().toUriString();
    }

    private String resolveBaseUrl() {
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://places-api.foursquare.com";
        }

        if (baseUrl.contains("api.foursquare.com/v3")) {
            return "https://places-api.foursquare.com";
        }

        return baseUrl;
    }

    private Map<String, Object> getForMap(String uri) {
        try {
            log.info("Foursquare request: {}", uri);
            return webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("X-Places-Api-Version", API_VERSION)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("Foursquare HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.warn("Foursquare call failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parsvane na Foursquare Places API response
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

                    String fsqPlaceId = (String) result.get("fsq_place_id");
                    if (fsqPlaceId != null) {
                        try {
                            List<String> photos = getPlacePhotos(fsqPlaceId);
                            if (!photos.isEmpty()) {
                                place.setWebsite(photos.get(0));
                            }
                        } catch (Exception e) {
                            log.debug("Could not load photos for place: {}", fsqPlaceId);
                        }
                    }
                }
            }
        }

        return places;
    }

    /**
     * Parsvane na otdelno myasto ot Foursquare JSON
     */
    private Place parsePlace(Map<String, Object> json) {
        try {
            Place place = new Place();

            String fsqPlaceId = (String) json.get("fsq_place_id");
            place.setGooglePlaceId(fsqPlaceId);
            place.setName((String) json.get("name"));

            Object lat = json.get("latitude");
            Object lng = json.get("longitude");
            if (lat != null) place.setLatitude(((Number) lat).doubleValue());
            if (lng != null) place.setLongitude(((Number) lng).doubleValue());

            Map<String, Object> location = (Map<String, Object>) json.get("location");
            if (location != null) {
                String address = (String) location.get("formatted_address");
                if (address == null) {
                    address = (String) location.get("address");
                }
                place.setAddress(address);
            }

            if (json.containsKey("rating")) {
                Object ratingObj = json.get("rating");
                if (ratingObj != null) {
                    double fsqRating = ((Number) ratingObj).doubleValue();
                    place.setRating(fsqRating / 2.0);
                }
            }

            Map<String, Object> stats = (Map<String, Object>) json.get("stats");
            if (stats != null && stats.containsKey("total_ratings")) {
                place.setUserRatingsTotal(((Number) stats.get("total_ratings")).intValue());
            }

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

            Map<String, Object> hours = (Map<String, Object>) json.get("hours");
            if (hours != null) {
                List<Map<String, Object>> regularHours = (List<Map<String, Object>>) hours.get("regular");
                if (regularHours != null && !regularHours.isEmpty()) {
                    place.setCurrentlyOpen(true);
                }
            }

            if (json.containsKey("tel")) {
                place.setPhoneNumber((String) json.get("tel"));
            }
            if (json.containsKey("website")) {
                place.setWebsite((String) json.get("website"));
            }

            return place;
        } catch (Exception e) {
            log.warn("Error parsing place: {}", e.getMessage());
            return null;
        }
    }
}
