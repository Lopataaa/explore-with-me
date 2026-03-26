package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    List<Request> findByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    @Query("SELECT r FROM Request r WHERE r.event.id IN :eventIds")
    List<Request> findByEventIds(@Param("eventIds") List<Long> eventIds);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByIdIn(List<Long> ids);

    @Query("SELECT r FROM Request r WHERE r.event.id = :eventId AND r.event.initiator.id = :userId")
    List<Request> findByEventIdAndEventInitiatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);
}