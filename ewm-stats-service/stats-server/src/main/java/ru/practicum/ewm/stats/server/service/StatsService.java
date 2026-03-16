package ru.practicum.ewm.stats.server.service;

import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.dto.ViewStats;
import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    /**
     * Сохранение информации о запросе к эндпоинту
     */
    void saveHit(EndpointHit endpointHit);

    /**
     * Получение статистики по посещениям
     * @param start начало диапазона
     * @param end конец диапазона
     * @param uris список URI для фильтрации (если null - все URI)
     * @param unique учитывать только уникальные IP
     * @return список статистики
     */
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                             List<String> uris, boolean unique);
}
