package ru.practicum.main.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;

    /**
     * Создание запроса на участие в событии
     */
    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        log.info("POST /users/{}/requests - Adding request for event: {}", userId, eventId);
        ParticipationRequestDto created = requestService.addRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Отмена запроса на участие в событии
     */
    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - Cancelling request", userId, requestId);
        ParticipationRequestDto cancelled = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Получение списка запросов текущего пользователя
     */
    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        log.info("GET /users/{}/requests - Getting user requests", userId);
        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }
}