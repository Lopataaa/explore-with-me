package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventUserRequest;
import ru.practicum.main.event.service.EventService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;

    /**
     * Получение событий текущего пользователя
     */
    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /users/{}/events - Getting events for user with from: {}, size: {}", userId, from, size);
        List<EventShortDto> events = eventService.getUserEvents(userId, from, size);
        return ResponseEntity.ok(events);
    }

    /**
     * Добавление нового события
     */
    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        log.info("POST /users/{}/events - Adding new event: {}", userId, newEventDto);
        EventFullDto createdEvent = eventService.addEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Получение полной информации о событии текущего пользователя
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("GET /users/{}/events/{} - Getting event by id", userId, eventId);
        EventFullDto event = eventService.getUserEventById(userId, eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Изменение события текущего пользователя
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {

        log.info("PATCH /users/{}/events/{} - Updating event with request: {}", userId, eventId, request);
        EventFullDto updatedEvent = eventService.updateUserEvent(userId, eventId, request);
        return ResponseEntity.ok(updatedEvent);
    }
}