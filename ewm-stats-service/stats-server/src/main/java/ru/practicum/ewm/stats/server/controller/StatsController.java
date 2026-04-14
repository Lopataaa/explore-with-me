package ru.practicum.ewm.stats.server.controller;

import ru.dto.EndpointHit;
import ru.dto.ViewStats;
import ru.practicum.ewm.stats.server.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHit endpointHit) {
        log.info("Получен POST запрос на /hit: {}", endpointHit);
        statsService.saveHit(endpointHit);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        log.info("Получен GET запрос на /stats: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        if (start == null || end == null) {
            return ResponseEntity.badRequest().build();
        }

        if (start.isAfter(end)) {
            log.warn("Start date {} is after end date {}", start, end);
            return ResponseEntity.badRequest().build();
        }

        List<ViewStats> stats = statsService.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }
}