package ru.client;

@Component
@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        restTemplate.postForObject(serverUrl + "/hit", hitDto, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        // Формирование URL с параметрами и запрос
    }
}
