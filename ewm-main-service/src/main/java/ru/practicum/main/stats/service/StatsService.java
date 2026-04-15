package ru.practicum.main.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.main.client.StatsClient;
import ru.practicum.main.client.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsClient statsClient;

    /**
     * Получение карты просмотров для списка URI
     *
     * @param start начало периода
     * @param end   конец периода
     * @param uris  список URI
     * @return карта URI -> количество просмотров
     */
    public Map<String, Long> getViewsMap(LocalDateTime start, LocalDateTime end, List<String> uris) {
        try {
            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);
            return stats.stream()
                    .collect(Collectors.toMap(
                            ViewStats::getUri,
                            ViewStats::getHits,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.error("Error getting views map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Получение количества просмотров для одного URI
     *
     * @param uri   URI события
     * @param start начало периода
     * @param end   конец периода
     * @return количество просмотров
     */
    public Long getViews(String uri, LocalDateTime start, LocalDateTime end) {
        try {
            List<ViewStats> stats = statsClient.getStats(start, end, List.of(uri), true);
            return stats.isEmpty() ? 0L : stats.get(0).getHits();
        } catch (Exception e) {
            log.error("Error getting views for {}: {}", uri, e.getMessage());
            return 0L;
        }
    }
}