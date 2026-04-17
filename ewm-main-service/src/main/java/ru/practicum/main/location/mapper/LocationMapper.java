package ru.practicum.main.location.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.LocationEntity;

@Component
public class LocationMapper {

    public LocationDto toLocationDto(LocationEntity location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .lat(location.getLat())
                .lon(location.getLon())
                .address(location.getAddress())
                .description(location.getDescription())
                .createdBy(location.getCreatedBy())
                .createdAt(location.getCreatedAt() != null ? location.getCreatedAt().toString() : null)
                .updatedAt(location.getUpdatedAt() != null ? location.getUpdatedAt().toString() : null)
                .build();
    }

    public ru.practicum.main.location.model.Location toLocation(ru.practicum.main.location.dto.LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        return ru.practicum.main.location.model.Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }

    public ru.practicum.main.location.dto.LocationDto toLocationDto(ru.practicum.main.location.model.Location location) {
        if (location == null) {
            return null;
        }

        return ru.practicum.main.location.dto.LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}