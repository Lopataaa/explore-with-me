package ru.practicum.ewm.stats.server.repository;

import ru.practicum.ewm.stats.server.model.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {

    // Интерфейс для проекции результата запроса
    interface ViewStatsProjection {
        String getApp();

        String getUri();

        Long getHits();
    }

    // Получение статистики с учётом всех просмотров (не уникальных)
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

    // Получение статистики только с уникальными IP
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