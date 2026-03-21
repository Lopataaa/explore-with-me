package ru.practicum.ewm.stats.server.service;

import ru.dto.EndpointHit;
import ru.dto.ViewStats;
import ru.practicum.ewm.stats.server.mapper.HitMapper;
import ru.practicum.ewm.stats.server.model.Hit;
import ru.practicum.ewm.stats.server.repository.StatsRepository;
import ru.practicum.ewm.stats.server.repository.StatsRepository.ViewStatsProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public void saveHit(EndpointHit endpointHit) {
        log.info("Сохранение статистики: app={}, uri={}, ip={}, timestamp={}",
                endpointHit.getApp(), endpointHit.getUri(),
                endpointHit.getIp(), endpointHit.getTimestamp());

        Hit hit = hitMapper.toEntity(endpointHit);
        statsRepository.save(hit);

        log.debug("Статистика сохранена с id={}", hit.getId());
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        log.info("Получение статистики за период с {} по {}, uris={}, unique={}",
                start, end, uris, unique);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        List<ViewStatsProjection> projections;
        if (unique) {
            projections = statsRepository.getUniqueStats(start, end, uris);
        } else {
            projections = statsRepository.getStats(start, end, uris);
        }

        List<ViewStats> result = projections.stream()
                .map(this::mapToViewStats)
                .collect(Collectors.toList());

        log.debug("Найдено {} записей статистики", result.size());
        return result;
    }

    private ViewStats mapToViewStats(ViewStatsProjection projection) {
        return new ViewStats(
                projection.getApp(),
                projection.getUri(),
                projection.getHits()
        );
    }
}
