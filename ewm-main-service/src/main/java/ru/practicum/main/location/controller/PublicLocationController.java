package ru.practicum.main.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.service.LocationService;

import java.util.List;

import static ru.practicum.main.location.constants.LocationConstants.DEFAULT_FROM;
import static ru.practicum.main.location.constants.LocationConstants.DEFAULT_SIZE;

/**
 * Публичный контроллер для просмотра локаций
 */
@Slf4j
@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class PublicLocationController {

    private final LocationService locationService;

    /**
     * Получение списка всех локаций
     *
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список локаций
     */
    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations(
            @RequestParam(defaultValue = "" + DEFAULT_FROM) Integer from,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) Integer size) {
        log.info("GET /locations - Getting all locations, from={}, size={}", from, size);
        List<LocationDto> result = locationService.getAllLocations(from, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Получение локации по ID
     *
     * @param id ID локации
     * @return локация
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        log.info("GET /locations/{} - Getting location by id", id);
        LocationDto result = locationService.getLocationById(id);
        return ResponseEntity.ok(result);
    }
}