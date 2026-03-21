package ru.practicum.main.location.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

@Component
public class LocationMapper {

    public Location toLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        return Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}