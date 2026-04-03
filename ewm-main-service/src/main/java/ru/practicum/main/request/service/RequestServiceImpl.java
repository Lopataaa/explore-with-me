package ru.practicum.main.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с запросами на участие в событиях
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    /**
     * Добавление запроса на участие в событии
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return DTO созданного запроса на участие
     * @throws NotFoundException если пользователь или событие не найдены
     * @throws ConflictException если:
     *                           - инициатор пытается подать заявку на своё событие
     *                           - событие не опубликовано
     *                           - запрос уже существует
     *                           - достигнут лимит участников
     */
    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Adding request for user: {} to event: {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request to own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event must be published");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        RequestStatus status;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        request = requestRepository.save(request);
        log.info("Request created with id: {}", request.getId());

        return requestMapper.toParticipationRequestDto(request);
    }

    /**
     * Отмена запроса на участие в событии
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор запроса
     * @return DTO отменённого запроса на участие
     * @throws NotFoundException если запрос не найден или не принадлежит пользователю
     */
    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Cancelling request: {} for user: {}", requestId, userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);

        return requestMapper.toParticipationRequestDto(request);
    }

    /**
     * Получение всех запросов пользователя на участие в чужих событиях
     *
     * @param userId идентификатор пользователя
     * @return список DTO запросов на участие
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение всех запросов на участие в событии текущего пользователя
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return список DTO запросов на участие
     * @throws NotFoundException если событие не найдено или пользователь не является инициатором
     */
    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event: {} by user: {}", eventId, userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        return requestRepository.findByEventId(eventId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Изменение статуса запросов на участие в событии (подтверждение/отклонение)
     * <p>
     * При подтверждении заявок:
     * <ul>
     *   <li>Если достигнут лимит участников, оставшиеся заявки автоматически отклоняются</li>
     *   <li>Если лимит не установлен (0), все заявки подтверждаются</li>
     * </ul>
     * При отклонении все выбранные заявки получают статус REJECTED
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @param request DTO с идентификаторами запросов и новым статусом
     * @return результат обработки заявок (подтверждённые и отклонённые)
     * @throws NotFoundException если событие не найдено или пользователь не является инициатором
     * @throws ConflictException если статус заявки не PENDING
     */
    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Changing request status for event: {} with request: {}", eventId, request);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<Request> requests = requestRepository.findAllById(request.getRequestIds());

        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        if (request.getStatus() == EventRequestStatusUpdateRequest.RequestStatusUpdate.CONFIRMED) {
            long currentConfirmed = event.getConfirmedRequests();
            int limit = event.getParticipantLimit();

            for (Request req : requests) {
                if (limit == 0 || currentConfirmed < limit) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(req);
                    currentConfirmed++;
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }

            event.setConfirmedRequests(currentConfirmed);
            eventRepository.save(event);

            if (limit > 0 && currentConfirmed >= limit) {
                List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                for (Request req : pendingRequests) {
                    if (!requests.contains(req)) {
                        req.setStatus(RequestStatus.REJECTED);
                        rejected.add(req);
                    }
                }
            }
        } else {
            for (Request req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(req);
            }
        }

        requestRepository.saveAll(confirmed);
        requestRepository.saveAll(rejected);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream()
                        .map(requestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(rejected.stream()
                        .map(requestMapper::toParticipationRequestDto)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Получение количества подтверждённых запросов на участие в событии
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых запросов
     */
    @Override
    public Long getConfirmedRequests(Long eventId) {
        log.info("Getting confirmed requests count for event: {}", eventId);
        return 0L;
    }

    /**
     * Получение всех запросов для списка событий
     *
     * @param eventIds список идентификаторов событий
     * @return список DTO запросов на участие
     */
    @Override
    public List<ParticipationRequestDto> getRequestsByEventIds(List<Long> eventIds) {
        log.info("Getting requests for event ids: {}", eventIds);

        if (eventIds == null || eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        return requestRepository.findByEventIds(eventIds)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}