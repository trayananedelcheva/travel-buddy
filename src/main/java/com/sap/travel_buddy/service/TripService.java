package com.sap.travel_buddy.service;

import com.sap.travel_buddy.domain.Place;
import com.sap.travel_buddy.domain.Trip;
import com.sap.travel_buddy.domain.User;
import com.sap.travel_buddy.domain.WeatherData;
import com.sap.travel_buddy.dto.CreateTripRequest;
import com.sap.travel_buddy.dto.TripDto;
import com.sap.travel_buddy.mapper.TripMapper;
import com.sap.travel_buddy.repository.PlaceRepository;
import com.sap.travel_buddy.repository.TripRepository;
import com.sap.travel_buddy.repository.WeatherDataRepository;
import com.sap.travel_buddy.service.external.OpenStreetMapService;
import com.sap.travel_buddy.service.external.WeatherService;
import com.sap.travel_buddy.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic за работа с разходки
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final TripMapper tripMapper;
    private final OpenStreetMapService openStreetMapService;
    private final WeatherService weatherService;
    private final PlaceService placeService;

    /**
     * Създаване на нова разходка
     */
    @Transactional
    public TripDto createTrip(CreateTripRequest request) {
        log.info("Creating trip: {}", request.getName());

        // Взимане на текущия потребител
        User currentUser = SecurityUtil.getCurrentUser();

        Trip trip = new Trip();
        trip.setUser(currentUser); // Задаване на потребителя
        trip.setName(request.getName());
        trip.setPlannedStartTime(request.getPlannedStartTime());
        trip.setPlannedEndTime(request.getPlannedEndTime());
        trip.setStatus(Trip.TripStatus.PLANNED);

        // Търсене и добавяне на места
        List<Place> places = new ArrayList<>();
        if (request.getPlaceSearchQueries() != null) {
            for (String query : request.getPlaceSearchQueries()) {
                List<Place> foundPlaces = openStreetMapService.searchPlacesByText(
                    query,
                    request.getStartLatitude(),
                    request.getStartLongitude(),
                    5000 // 5km radius
                );
                
                if (!foundPlaces.isEmpty()) {
                    Place place = foundPlaces.get(0); // Взимаме първия резултат
                    place = placeService.saveOrUpdatePlace(place);
                    places.add(place);
                }
            }
        }
        trip.setPlaces(places);

        // Взимане на прогноза за първото място или стартовата локация
        if (!places.isEmpty()) {
            Place firstPlace = places.get(0);
            WeatherData weatherData = weatherService.getForecast(
                firstPlace.getLatitude(),
                firstPlace.getLongitude(),
                request.getPlannedStartTime()
            );
            
            if (weatherData != null) {
                weatherData = weatherDataRepository.save(weatherData);
                trip.setWeatherData(weatherData);
            }
        } else if (request.getStartLatitude() != null && request.getStartLongitude() != null) {
            WeatherData weatherData = weatherService.getForecast(
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getPlannedStartTime()
            );
            
            if (weatherData != null) {
                weatherData = weatherDataRepository.save(weatherData);
                trip.setWeatherData(weatherData);
            }
        }

        trip = tripRepository.save(trip);
        
        log.info("Trip created with ID: {}", trip.getId());
        return tripMapper.toDto(trip);
    }

    /**
     * Взимане на разходка по ID
     */
    public Optional<TripDto> getTripById(Long id) {
        return tripRepository.findById(id)
            .map(tripMapper::toDto);
    }

    /**
     * Взимане на всички разходки
     */
    public List<TripDto> getAllTrips() {
        return tripRepository.findAll().stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на предстоящи разходки
     */
    public List<TripDto> getUpcomingTrips() {
        return tripRepository.findUpcomingTrips(LocalDateTime.now()).stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на разходки по статус
     */
    public List<TripDto> getTripsByStatus(Trip.TripStatus status) {
        return tripRepository.findByStatus(status).stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на препоръчани разходки
     */
    public List<TripDto> getRecommendedTrips() {
        return tripRepository.findByIsRecommendedTrue().stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Обновяване на статус на разходка
     */
    @Transactional
    public Optional<TripDto> updateTripStatus(Long id, Trip.TripStatus newStatus) {
        return tripRepository.findById(id)
            .map(trip -> {
                trip.setStatus(newStatus);
                trip = tripRepository.save(trip);
                log.info("Trip {} status updated to {}", id, newStatus);
                return tripMapper.toDto(trip);
            });
    }

    /**
     * Добавяне на място към разходка
     */
    @Transactional
    public Optional<TripDto> addPlaceToTrip(Long tripId, Long placeId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        Optional<Place> placeOpt = placeRepository.findById(placeId);

        if (tripOpt.isPresent() && placeOpt.isPresent()) {
            Trip trip = tripOpt.get();
            Place place = placeOpt.get();
            
            if (!trip.getPlaces().contains(place)) {
                trip.getPlaces().add(place);
                trip = tripRepository.save(trip);
                log.info("Place {} added to trip {}", placeId, tripId);
            }
            
            return Optional.of(tripMapper.toDto(trip));
        }

        return Optional.empty();
    }

    /**
     * Премахване на място от разходка
     */
    @Transactional
    public Optional<TripDto> removePlaceFromTrip(Long tripId, Long placeId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        Optional<Place> placeOpt = placeRepository.findById(placeId);

        if (tripOpt.isPresent() && placeOpt.isPresent()) {
            Trip trip = tripOpt.get();
            Place place = placeOpt.get();
            
            trip.getPlaces().remove(place);
            trip = tripRepository.save(trip);
            log.info("Place {} removed from trip {}", placeId, tripId);
            
            return Optional.of(tripMapper.toDto(trip));
        }

        return Optional.empty();
    }

    /**
     * Обновяване на прогнозата за разходка
     */
    @Transactional
    public Optional<TripDto> refreshWeatherForTrip(Long tripId) {
        return tripRepository.findById(tripId)
            .map(trip -> {
                if (!trip.getPlaces().isEmpty()) {
                    Place firstPlace = trip.getPlaces().get(0);
                    WeatherData weatherData = weatherService.getForecast(
                        firstPlace.getLatitude(),
                        firstPlace.getLongitude(),
                        trip.getPlannedStartTime()
                    );
                    
                    if (weatherData != null) {
                        weatherData = weatherDataRepository.save(weatherData);
                        trip.setWeatherData(weatherData);
                        trip = tripRepository.save(trip);
                    }
                }
                
                return tripMapper.toDto(trip);
            });
    }

    /**
     * Изтриване на разходка
     */
    @Transactional
    public void deleteTrip(Long id) {
        tripRepository.deleteById(id);
        log.info("Trip {} deleted", id);
    }

    /**
     * Търсене на разходки по име
     */
    public List<TripDto> searchTripsByName(String name) {
        return tripRepository.findByNameContainingIgnoreCase(name).stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на всички разходки на текущия потребител
     */
    public List<TripDto> getCurrentUserTrips() {
        User currentUser = SecurityUtil.getCurrentUser();
        return tripRepository.findByUserOrderByPlannedStartTimeDesc(currentUser.getId()).stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Взимане на разходки на текущия потребител по статус
     */
    public List<TripDto> getCurrentUserTripsByStatus(Trip.TripStatus status) {
        User currentUser = SecurityUtil.getCurrentUser();
        return tripRepository.findByUserAndStatus(currentUser.getId(), status).stream()
            .map(tripMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Брой разходки на текущия потребител
     */
    public long getCurrentUserTripsCount() {
        User currentUser = SecurityUtil.getCurrentUser();
        return tripRepository.findByUserOrderByPlannedStartTimeDesc(currentUser.getId()).size();
    }
}
