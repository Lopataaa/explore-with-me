package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.model.EventState;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления событиями
 */
public interface EventService {

    /**
     * Добавление нового события
     *
     * @param userId      ID пользователя, добавляющего событие
     * @param newEventDto DTO с данными для создания события
     * @return DTO с полной информацией о созданном событии
     */
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    /**
     * Получение списка событий пользователя
     *
     * @param userId ID пользователя
     * @param from   количество элементов, которое нужно пропустить (для пагинации)
     * @param size   количество элементов на странице
     * @return список DTO с краткой информацией о событиях пользователя
     */
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    /**
     * Получение полной информации о событии пользователя по его ID
     *
     * @param userId  ID пользователя
     * @param eventId ID события
     * @return DTO с полной информацией о событии
     */
    EventFullDto getUserEventById(Long userId, Long eventId);

    /**
     * Обновление события пользователем
     *
     * @param userId  ID пользователя
     * @param eventId ID события
     * @param request DTO с данными для обновления события
     * @return DTO с обновленной информацией о событии
     */
    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    /**
     * Получение списка событий с фильтрацией для публичного доступа
     *
     * @param text          текст для поиска в аннотации и описании события
     * @param categories    список ID категорий для фильтрации
     * @param paid          фильтр по платным/бесплатным событиям
     * @param rangeStart    дата и время начала диапазона для поиска событий
     * @param rangeEnd      дата и время окончания диапазона для поиска событий
     * @param onlyAvailable только доступные события (не достигнут лимит участников)
     * @param sort          вариант сортировки (по дате или по просмотрам)
     * @param from          количество элементов, которое нужно пропустить (для пагинации)
     * @param size          количество элементов на странице
     * @param httpRequest   HTTP-запрос для получения информации о просмотрах
     * @return список DTO с краткой информацией о событиях
     */
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort,
                                        Integer from, Integer size, HttpServletRequest httpRequest);

    /**
     * Получение полной информации о событии для публичного доступа
     *
     * @param id          ID события
     * @param httpRequest HTTP-запрос для увеличения счетчика просмотров
     * @return DTO с полной информацией о событии
     */
    EventFullDto getPublicEventById(Long id, HttpServletRequest httpRequest);

    /**
     * Получение списка событий с фильтрацией для администратора
     *
     * @param users      список ID пользователей, чьи события нужно искать
     * @param states     список состояний событий для фильтрации
     * @param categories список ID категорий для фильтрации
     * @param rangeStart дата и время начала диапазона для поиска событий
     * @param rangeEnd   дата и время окончания диапазона для поиска событий
     * @param from       количество элементов, которое нужно пропустить (для пагинации)
     * @param size       количество элементов на странице
     * @return список DTO с полной информацией о событиях
     */
    List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                      Integer from, Integer size);

    /**
     * Обновление события администратором
     *
     * @param eventId ID события
     * @param request DTO с данными для обновления события
     * @return DTO с обновленной информацией о событии
     */
    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request);
}