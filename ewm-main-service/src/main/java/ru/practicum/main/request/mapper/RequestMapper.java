package ru.practicum.main.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.Request;

@Component
public class RequestMapper {

    /**
     * Преобразование сущности Request в ParticipationRequestDto
     *
     * @param request сущность запроса на участие
     * @return DTO запроса на участие
     */
    public ParticipationRequestDto toParticipationRequestDto(Request request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}