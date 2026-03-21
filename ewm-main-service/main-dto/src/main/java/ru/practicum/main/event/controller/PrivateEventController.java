package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /users/{}/events - Getting events for user with from: {}, size: {}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    /**
     * Добавление нового события
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        log.info("POST /users/{}/events - Adding new event: {}", userId, newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    /**
     * Получение полной информации о событии текущего пользователя
     */
    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("GET /users/{}/events/{} - Getting event by id", userId, eventId);
        return eventService.getUserEventById(userId, eventId);
    }

    /**
     * Изменение события текущего пользователя
     */
    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {

        log.info("PATCH /users/{}/events/{} - Updating event with request: {}", userId, eventId, request);
        return eventService.updateUserEvent(userId, eventId, request);
    }
}