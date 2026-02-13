package com.sap.travel_buddy.service.external;

import com.sap.travel_buddy.config.FoursquareConfig;
import com.sap.travel_buddy.domain.Place;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalTime;
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
    private static final String DETAILS_FIELDS = "hours,rating";

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
        log.info("[PlacesService] Foursquare searchByText uri={}", uri);
        Map<String, Object> response = getForMap(uri);
        List<Place> places = parseFoursquareResponse(response);
        log.info("[PlacesService] searchByText parsed places={}", places.size());
        return places;
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
        log.info("[PlacesService] Foursquare searchNearby uri={}", uri);
        Map<String, Object> response = getForMap(uri);
        List<Place> places = parseFoursquareResponse(response);
        log.info("[PlacesService] searchNearby parsed places={}", places.size());
        return places;
    }

    /**
     * Vzimane na detayli za konkretno myasto (po Foursquare fsq_place_id)
     */
    public Place getPlaceDetails(String placeId) {
        return getPlaceDetails(placeId, null);
    }

    public Place getPlaceDetails(String placeId, String fields) {
        if (placeId == null || placeId.isBlank()) {
            return null;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(resolveBaseUrl())
            .path("/places/" + placeId);

        if (fields != null && !fields.isBlank()) {
            builder.queryParam("fields", fields);
        }

        String uri = builder.build().toUriString();

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
                            enrichPlaceWithDetails(fsqPlaceId, place);
                        } catch (Exception e) {
                            log.debug("Could not enrich place details for: {}", fsqPlaceId);
                        }
                        try {
                            List<String> photos = getPlacePhotos(fsqPlaceId);
                            if (!photos.isEmpty()) {
                                place.setPhotoUrl(photos.get(0));
                            }
                        } catch (Exception e) {
                            log.debug("Could not load photos for place: {}", fsqPlaceId);
                        }
                    }
                }
            }
        }

        log.info("[PlacesService] parseFoursquareResponse results={} places={}",
            results != null ? results.size() : 0,
            places.size());
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
                place.setFormattedAddress((String) location.get("formatted_address"));
                place.setLocality((String) location.get("locality"));
                place.setRegion((String) location.get("region"));
                place.setCountry((String) location.get("country"));
                place.setPostcode((String) location.get("postcode"));
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
                List<String> categoryIds = new ArrayList<>();
                for (Object catObj : categories) {
                    Map<String, Object> cat = (Map<String, Object>) catObj;
                    String catName = (String) cat.get("name");
                    String catId = (String) cat.get("fsq_category_id");
                    if (catName != null) {
                        categoryNames.add(catName);
                    }
                    if (catId != null) {
                        categoryIds.add(catId);
                    }
                }
                place.setTypes(String.join(",", categoryNames));
                place.setCategoryIds(String.join(",", categoryIds));
            }

            Map<String, Object> hours = (Map<String, Object>) json.get("hours");
            if (hours != null) {
                Object openNow = hours.get("open_now");
                if (openNow instanceof Boolean) {
                    place.setCurrentlyOpen((Boolean) openNow);
                }

                List<Map<String, Object>> regularHours = (List<Map<String, Object>>) hours.get("regular");
                if (regularHours != null && !regularHours.isEmpty()) {
                    Map<String, Object> firstEntry = regularHours.get(0);
                    LocalTime openingTime = parseFoursquareTime(firstEntry.get("open"));
                    LocalTime closingTime = parseFoursquareTime(firstEntry.get("close"));

                    if (openingTime == null) {
                        openingTime = parseFoursquareTime(firstEntry.get("start"));
                    }
                    if (closingTime == null) {
                        closingTime = parseFoursquareTime(firstEntry.get("end"));
                    }

                    if (openingTime != null) {
                        place.setOpeningTime(openingTime);
                    }
                    if (closingTime != null) {
                        place.setClosingTime(closingTime);
                    }
                }
            }

            if (json.containsKey("tel")) {
                place.setPhoneNumber((String) json.get("tel"));
            }
            if (json.containsKey("website")) {
                place.setWebsite((String) json.get("website"));
            }
            if (json.containsKey("distance")) {
                Object distanceObj = json.get("distance");
                if (distanceObj != null) {
                    place.setDistanceMeters(((Number) distanceObj).intValue());
                }
            }
            if (json.containsKey("timezone")) {
                place.setTimezone((String) json.get("timezone"));
            }
            if (json.containsKey("link")) {
                place.setFsqLink((String) json.get("link"));
            }

            return place;
        } catch (Exception e) {
            log.warn("Error parsing place: {}", e.getMessage());
            return null;
        }
    }

    private void enrichPlaceWithDetails(String fsqPlaceId, Place place) {
        Place details = getPlaceDetails(fsqPlaceId, DETAILS_FIELDS);
        if (details == null) {
            return;
        }

        if (place.getRating() == null) {
            place.setRating(details.getRating());
        }
        if (place.getOpeningTime() == null) {
            place.setOpeningTime(details.getOpeningTime());
        }
        if (place.getClosingTime() == null) {
            place.setClosingTime(details.getClosingTime());
        }
        if (place.getCurrentlyOpen() == null) {
            place.setCurrentlyOpen(details.getCurrentlyOpen());
        }
    }

    private LocalTime parseFoursquareTime(Object value) {
        if (value == null) {
            return null;
        }

        String raw = value.toString().trim();
        if (raw.isEmpty()) {
            return null;
        }

        String normalized = raw.replace(":", "");
        if (normalized.length() != 4) {
            return null;
        }

        try {
            int hour = Integer.parseInt(normalized.substring(0, 2));
            int minute = Integer.parseInt(normalized.substring(2, 4));
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }
}
