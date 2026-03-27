package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.client.StatsClient;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.request.service.RequestService;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления событиями
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestService requestService;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    /**
     * Добавление нового события
     *
     * @param userId      идентификатор пользователя-инициатора
     * @param newEventDto данные для создания события
     * @return DTO с полной информацией о созданном событии
     * @throws NotFoundException если пользователь или категория не найдены
     * @throws ConflictException если дата события не соответствует правилам
     */
    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Adding event for user: {}", userId);

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        if (newEventDto.getTitle() == null || newEventDto.getTitle().length() < 3) {
            throw new BadRequestException("Title length must be at least 3");
        }
        if (newEventDto.getTitle().length() > 120) {
            throw new BadRequestException("Title length must be no more than 120");
        }

        if (newEventDto.getAnnotation() == null || newEventDto.getAnnotation().length() < 20) {
            throw new BadRequestException("Annotation length must be at least 20");
        }
        if (newEventDto.getAnnotation().length() > 2000) {
            throw new BadRequestException("Annotation length must be no more than 2000");
        }

        if (newEventDto.getDescription() == null || newEventDto.getDescription().length() < 20) {
            throw new BadRequestException("Description length must be at least 20");
        }
        if (newEventDto.getDescription().length() > 7000) {
            throw new BadRequestException("Description length must be no more than 7000");
        }

        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit must be greater than or equal to 0");
        }

        if (newEventDto.getCategory() == null) {
            throw new BadRequestException("Category must not be null");
        }

        if (newEventDto.getLocation() == null) {
            throw new BadRequestException("Location must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setViews(0L);

        event = eventRepository.save(event);
        log.info("Event created with id: {}", event.getId());

        return eventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), event.getViews());
    }

    /**
     * Получение списка событий пользователя
     *
     * @param userId идентификатор пользователя
     * @param from   количество элементов для пропуска
     * @param size   количество элементов на странице
     * @return список DTO с краткой информацией о событиях
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        return events.getContent().stream()
                .map(event -> eventMapper.toEventShortDto(event, getConfirmedRequests(event.getId()), event.getViews()))
                .collect(Collectors.toList());
    }

    /**
     * Получение полной информации о событии пользователя
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return DTO с полной информацией о событии
     * @throws NotFoundException если событие не найдено
     */
    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        log.info("Getting event {} for user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return eventMapper.toEventFullDto(event, getConfirmedRequests(eventId), event.getViews());
    }

    /**
     * Обновление события пользователем
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @param request данные для обновления
     * @return DTO с обновленной информацией о событии
     * @throws NotFoundException если событие не найдено
     * @throws BadRequestException если данные не проходят валидацию
     * @throws ConflictException если событие нельзя редактировать
     */
    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Updating event {} for user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getEventDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime eventDate = request.getEventDate();

            if (eventDate.isBefore(now)) {
                throw new BadRequestException("Event date must be in the future");
            }
            if (eventDate.isBefore(now.plusHours(MIN_HOURS_BEFORE_EVENT))) {
                throw new BadRequestException("Event date must be at least 2 hours from now");
            }
        }

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new BadRequestException("Title must not be blank");
            }
            if (request.getTitle().length() < 3) {
                throw new BadRequestException("Title length must be at least 3");
            }
            if (request.getTitle().length() > 120) {
                throw new BadRequestException("Title length must be no more than 120");
            }
        }

        if (request.getAnnotation() != null) {
            if (request.getAnnotation().isBlank()) {
                throw new BadRequestException("Annotation must not be blank");
            }
            if (request.getAnnotation().length() < 20) {
                throw new BadRequestException("Annotation length must be at least 20");
            }
            if (request.getAnnotation().length() > 2000) {
                throw new BadRequestException("Annotation length must be no more than 2000");
            }
        }

        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) {
                throw new BadRequestException("Description must not be blank");
            }
            if (request.getDescription().length() < 20) {
                throw new BadRequestException("Description length must be at least 20");
            }
            if (request.getDescription().length() > 7000) {
                throw new BadRequestException("Description length must be no more than 7000");
            }
        }

        if (request.getParticipantLimit() != null && request.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit must be greater than or equal to 0");
        }

        if (request.getLocation() != null) {
            if (request.getLocation().getLat() == null || request.getLocation().getLon() == null) {
                throw new BadRequestException("Location must have lat and lon");
            }
        }

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(request.getLocation()));
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UpdateEventUserRequest.UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (request.getStateAction() == UpdateEventUserRequest.UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        event = eventRepository.save(event);
        log.info("Event updated: {}", event.getId());

        Long confirmedRequests = getConfirmedRequests(eventId);

        return eventMapper.toEventFullDto(event, confirmedRequests, event.getViews());
    }

    /**
     * Получение публичных событий с фильтрацией
     *
     * @param text          текст для поиска
     * @param categories    список категорий
     * @param paid          флаг платности
     * @param rangeStart    начало диапазона дат
     * @param rangeEnd      конец диапазона дат
     * @param onlyAvailable только доступные события
     * @param sort          сортировка
     * @param from          количество элементов для пропуска
     * @param size          количество элементов на странице
     * @param httpRequest   HTTP-запрос для статистики
     * @return список DTO с краткой информацией о событиях
     */
    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               Integer from, Integer size, HttpServletRequest httpRequest) {
        log.info("Getting public events");

        try {
            if (rangeStart == null) {
                rangeStart = LocalDateTime.now();
            }
            if (rangeEnd == null) {
                rangeEnd = LocalDateTime.now().plusYears(10);
            }

            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("rangeStart must be before rangeEnd");
            }

            Pageable pageable;
            if (sort != null && sort.equals("VIEWS")) {
                pageable = PageRequest.of(from / size, size, Sort.by("views").descending());
            } else {
                pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
            }

            Page<Event> events = eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd, pageable);

            log.info("Found {} events", events.getTotalElements());

            // statsClient временно отключен для отладки
            // try {
            //     statsClient.saveHit("ewm-main-service", httpRequest.getRequestURI(),
            //             httpRequest.getRemoteAddr(), LocalDateTime.now());
            // } catch (Exception e) {
            //     log.warn("Failed to save stats: {}", e.getMessage());
            // }

            return events.getContent().stream()
                    .map(event -> eventMapper.toEventShortDto(event, 0L, event.getViews()))
                    .collect(Collectors.toList());

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting public events: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получение публичного события по идентификатору
     *
     * @param id          идентификатор события
     * @param httpRequest HTTP-запрос для статистики
     * @return DTO с полной информацией о событии
     * @throws NotFoundException если событие не найдено или не опубликовано
     */
    @Override
    public EventFullDto getPublicEventById(Long id, HttpServletRequest httpRequest) {
        log.info("Getting public event by id: {}", id);

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        statsClient.saveHit("ewm-main-service", httpRequest.getRequestURI(),
                httpRequest.getRemoteAddr(), LocalDateTime.now());

        event.setViews(event.getViews() + 1);
        event = eventRepository.save(event);

        return eventMapper.toEventFullDto(event, getConfirmedRequests(id), event.getViews());
    }

    /**
     * Получение событий для администратора с фильтрацией
     *
     * @param users      список идентификаторов пользователей
     * @param states     список статусов
     * @param categories список категорий
     * @param rangeStart начало диапазона дат
     * @param rangeEnd   конец диапазона дат
     * @param from       количество элементов для пропуска
     * @param size       количество элементов на странице
     * @return список DTO с полной информацией о событиях
     */
    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Integer from, Integer size) {
        log.info("Getting admin events");

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findAdminEvents(users, states, categories, rangeStart, rangeEnd, pageable);

        return events.getContent().stream()
                .map(event -> eventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), event.getViews()))
                .collect(Collectors.toList());
    }

    /**
     * Обновление события администратором
     *
     * @param eventId идентификатор события
     * @param request данные для обновления
     * @return DTO с обновленной информацией о событии
     * @throws NotFoundException если событие не найдено
     * @throws ConflictException если событие нельзя опубликовать или отклонить
     */
    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        log.info("Updating admin event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getTitle() != null) {
            if (request.getTitle().length() < 3) {
                throw new BadRequestException("Title length must be at least 3");
            }
            if (request.getTitle().length() > 120) {
                throw new BadRequestException("Title length must be no more than 120");
            }
        }

        if (request.getAnnotation() != null) {
            if (request.getAnnotation().length() < 20) {
                throw new BadRequestException("Annotation length must be at least 20");
            }
            if (request.getAnnotation().length() > 2000) {
                throw new BadRequestException("Annotation length must be no more than 2000");
            }
        }

        if (request.getDescription() != null) {
            if (request.getDescription().length() < 20) {
                throw new BadRequestException("Description length must be at least 20");
            }
            if (request.getDescription().length() > 7000) {
                throw new BadRequestException("Description length must be no more than 7000");
            }
        }

        if (request.getParticipantLimit() != null && request.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit must be greater than or equal to 0");
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(request.getLocation()));
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UpdateEventAdminRequest.AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour from now");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction() == UpdateEventAdminRequest.AdminStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        event = eventRepository.save(event);
        log.info("Admin event updated: {}", eventId);

        return eventMapper.toEventFullDto(event, getConfirmedRequests(eventId), event.getViews());
    }

    /**
     * Получение количества подтвержденных запросов на участие в событии
     *
     * @param eventId идентификатор события
     * @return количество подтвержденных запросов
     */
    private Long getConfirmedRequests(Long eventId) {
        try {
            return requestService.getConfirmedRequests(eventId);
        } catch (Exception e) {
            log.warn("Failed to get confirmed requests for event {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}