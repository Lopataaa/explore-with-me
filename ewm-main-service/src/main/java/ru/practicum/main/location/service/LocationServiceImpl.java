package ru.practicum.main.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.location.dto.CreateLocationRequest;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.location.model.LocationEntity;
import ru.practicum.main.location.repository.LocationRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main.location.constants.LocationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final LocationMapper locationMapper;

    private void validateCoordinates(Float lat, Float lon) {
        if (lat == null || lon == null) {
            throw new BadRequestException("Latitude and longitude must not be null");
        }
        if (lat < MIN_LAT || lat > MAX_LAT) {
            throw new BadRequestException(String.format(INVALID_COORDINATES, lat, lon));
        }
        if (lon < MIN_LON || lon > MAX_LON) {
            throw new BadRequestException(String.format(INVALID_COORDINATES, lat, lon));
        }
    }

    @Override
    @Transactional
    public LocationDto createLocation(Long adminId, CreateLocationRequest request) {
        log.info("Admin {} creating location: {}", adminId, request.getName());

        validateCoordinates(request.getLat(), request.getLon());

        if (locationRepository.existsByName(request.getName())) {
            throw new ConflictException(String.format(LOCATION_NAME_EXISTS, request.getName()));
        }

        LocationEntity location = LocationEntity.builder()
                .name(request.getName())
                .lat(request.getLat())
                .lon(request.getLon())
                .address(request.getAddress())
                .description(request.getDescription())
                .createdBy(adminId)
                .build();

        location = locationRepository.save(location);
        log.info("Location created with id: {}", location.getId());

        return locationMapper.toLocationDto(location);
    }

    @Override
    @Transactional
    public LocationDto updateLocation(Long adminId, Long locationId, CreateLocationRequest request) {
        log.info("Admin {} updating location: {}", adminId, locationId);

        validateCoordinates(request.getLat(), request.getLon());

        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException(String.format(LOCATION_NOT_FOUND, locationId)));

        if (!location.getName().equals(request.getName()) && locationRepository.existsByName(request.getName())) {
            throw new ConflictException(String.format(LOCATION_NAME_EXISTS, request.getName()));
        }

        location.setName(request.getName());
        location.setLat(request.getLat());
        location.setLon(request.getLon());
        location.setAddress(request.getAddress());
        location.setDescription(request.getDescription());

        location = locationRepository.save(location);
        log.info("Location updated: {}", locationId);

        return locationMapper.toLocationDto(location);
    }

    @Override
    @Transactional
    public void deleteLocation(Long adminId, Long locationId) {
        log.info("Admin {} deleting location: {}", adminId, locationId);

        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new NotFoundException(String.format(LOCATION_NOT_FOUND, locationId)));

//        boolean isUsedByEvents = eventRepository.existsByLocationId(locationId);
//        if (isUsedByEvents) {
//            throw new ConflictException(CANNOT_DELETE_LOCATION_IN_USE);
//        }

        locationRepository.delete(location);
        log.info("Location deleted: {}", locationId);
    }

    @Override
    public LocationDto getLocationById(Long id) {
        log.info("Getting location by id: {}", id);

        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(LOCATION_NOT_FOUND, id)));

        return locationMapper.toLocationDto(location);
    }

    @Override
    public List<LocationDto> getAllLocations(Integer from, Integer size) {
        log.info("Getting all locations, from={}, size={}", from, size);

        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return locationRepository.findAll(pageable).stream()
                .map(locationMapper::toLocationDto)
                .collect(Collectors.toList());
    }
}
