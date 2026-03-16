package ru.practicum.ewm.stats.server.repository;

import ru.practicum.ewm.stats.server.model.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы со статистикой посещений
 */
@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {

    /**
     * Проекция для получения агрегированной статистики
     */
    interface ViewStatsProjection {
        String getApp();
        String getUri();
        Long getHits();
    }

    /**
     * Возвращает статистику по всем просмотрам (без учета уникальности IP)
     *
     * @param start начало временного диапазона
     * @param end   конец временного диапазона
     * @param uris  список URI для фильтрации (если null - без фильтрации)
     * @return список проекций со статистикой, сгруппированный по app и uri
     */
    @Query("SELECT h.app as app, h.uri as uri, COUNT(h) as hits " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    /**
     * Возвращает статистику только по уникальным IP-адресам
     *
     * @param start начало временного диапазона
     * @param end   конец временного диапазона
     * @param uris  список URI для фильтрации (если null - без фильтрации)
     * @return список проекций со статистикой, сгруппированный по app и uri
     */
    @Query("SELECT h.app as app, h.uri as uri, COUNT(DISTINCT h.ip) as hits " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsProjection> getUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);
}