package ru.practicum.main.request.service;

import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для работы с запросами на участие в событиях
 */
public interface RequestService {

    /**
     * Создание запроса на участие в событии
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return DTO созданного запроса
     */
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    /**
     * Отмена запроса на участие в событии
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор запроса
     * @return DTO отмененного запроса
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    /**
     * Получение списка запросов текущего пользователя
     *
     * @param userId идентификатор пользователя
     * @return список DTO запросов пользователя
     */
    List<ParticipationRequestDto> getUserRequests(Long userId);

    /**
     * Получение списка запросов на участие в событии
     *
     * @param userId  идентификатор пользователя (инициатора события)
     * @param eventId идентификатор события
     * @return список DTO запросов на участие
     */
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    /**
     * Изменение статуса запросов на участие в событии
     *
     * @param userId  идентификатор пользователя (инициатора события)
     * @param eventId идентификатор события
     * @param request DTO с информацией о запросах и новом статусе
     * @return результат обновления статусов
     */
    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    /**
     * Получение количества подтвержденных запросов для события
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных запросов
     */
    Long getConfirmedRequests(Long eventId);

    /**
     * Получение списка запросов для списка событий
     *
     * @param eventIds список идентификаторов событий
     * @return список DTO запросов
     */
    List<ParticipationRequestDto> getRequestsByEventIds(List<Long> eventIds);
}