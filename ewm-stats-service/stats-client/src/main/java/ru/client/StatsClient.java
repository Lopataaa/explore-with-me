package ru.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;  // Добавлен импорт
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dto.EndpointHitDto;
import ru.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate;

    @Value("${stats-server.url}")
    private String serverUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        try {
            EndpointHitDto hitDto = EndpointHitDto.builder()
                    .app(app)
                    .uri(uri)
                    .ip(ip)
                    .timestamp(timestamp)
                    .build();

            restTemplate.postForEntity(serverUrl + "/hit", hitDto, Void.class);
            log.info("Stats saved: app={}, uri={}, ip={}", app, uri, ip);
        } catch (Exception e) {
            log.error("Error saving stats: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER))
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            String url = builder.toUriString();
            log.info("Requesting stats from: {}", url);

            ResponseEntity<List<ViewStats>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStats>>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return List.of();
        }
    }

    public Long getViews(String uri, LocalDateTime start, LocalDateTime end) {
        List<ViewStats> stats = getStats(start, end, List.of(uri), true);
        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }
}