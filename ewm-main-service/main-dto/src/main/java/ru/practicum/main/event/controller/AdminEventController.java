package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.service.EventService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    /**
     * Поиск событий (административный)
     */
    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users, @RequestParam(required = false) List<EventState> states, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd, @RequestParam(defaultValue = "0") @PositiveOrZero Integer from, @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /admin/events - Getting events with users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}", users, states, categories, rangeStart, rangeEnd, from, size);

        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    /**
     * Редактирование события (административное)
     */
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest request) {

        log.info("PATCH /admin/events/{} - Updating event with request: {}", eventId, request);
        return eventService.updateAdminEvent(eventId, request);
    }
}