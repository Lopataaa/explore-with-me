package ru.practicum.main.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.main.client.dto.EndpointHitDto;
import ru.practicum.main.client.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);

            restTemplate.postForEntity(serverUrl + "/hit", request, Void.class);
            log.info("Stats saved: app={}, uri={}, ip={}", app, uri, ip);
        } catch (Exception e) {
            log.error("Error saving stats: {}", e.getMessage());
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        try {
            String encodedStart = URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8);
            String encodedEnd = URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", encodedStart)
                    .queryParam("end", encodedEnd)
                    .queryParam("unique", unique);

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            ResponseEntity<ViewStats[]> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    ViewStats[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Long getViews(String uri, LocalDateTime start, LocalDateTime end) {
        List<ViewStats> stats = getStats(start, end, List.of(uri), false);
        if (stats == null || stats.isEmpty()) {
            return 0L;
        }
        return stats.get(0).getHits();
    }
}