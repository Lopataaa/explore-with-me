package ru.practicum.main.location.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.location.dto.CreateLocationRequest;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.service.LocationService;

/**
 * Контроллер для управления локациями (административный доступ)
 */
@Slf4j
@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class AdminLocationController {

    private final LocationService locationService;

    /**
     * Создание новой локации
     *
     * @param userId  ID администратора
     * @param request данные для создания локации
     * @return созданная локация
     */
    @PostMapping
    public ResponseEntity<LocationDto> createLocation(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateLocationRequest request) {
        log.info("POST /admin/locations - Creating location by admin: {}", userId);
        LocationDto result = locationService.createLocation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Обновление локации
     *
     * @param userId     ID администратора
     * @param locationId ID локации
     * @param request    данные для обновления
     * @return обновленная локация
     */
    @PutMapping("/{locationId}")
    public ResponseEntity<LocationDto> updateLocation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long locationId,
            @Valid @RequestBody CreateLocationRequest request) {
        log.info("PUT /admin/locations/{} - Updating location by admin: {}", locationId, userId);
        LocationDto result = locationService.updateLocation(userId, locationId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * Удаление локации
     *
     * @param userId     ID администратора
     * @param locationId ID локации
     * @return статус 204 No Content
     */
    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long locationId) {
        log.info("DELETE /admin/locations/{} - Deleting location by admin: {}", locationId, userId);
        locationService.deleteLocation(userId, locationId);
        return ResponseEntity.noContent().build();
    }
}