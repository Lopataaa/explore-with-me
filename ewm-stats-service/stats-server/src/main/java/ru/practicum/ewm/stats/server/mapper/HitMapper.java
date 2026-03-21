package ru.practicum.ewm.stats.server.mapper;

import ru.dto.EndpointHit;
import ru.practicum.ewm.stats.server.model.Hit;
import org.springframework.stereotype.Component;

@Component
public class HitMapper {

    public Hit toEntity(EndpointHit endpointHit) {
        return Hit.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }

    public EndpointHit toDto(Hit hit) {
        EndpointHit dto = new EndpointHit();
        dto.setApp(hit.getApp());
        dto.setUri(hit.getUri());
        dto.setIp(hit.getIp());
        dto.setTimestamp(hit.getTimestamp());
        return dto;
    }
}