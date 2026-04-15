package ru.practicum.main.compilation.service;

import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

/**
 * Сервис для работы с подборками событий
 */
public interface CompilationService {

    /**
     * Создание новой подборки событий
     *
     * @param newCompilationDto DTO с данными для создания подборки
     * @return DTO созданной подборки
     * @throws ru.practicum.main.exception.NotFoundException если событие из подборки не найдено
     */
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    /**
     * Обновление подборки событий
     *
     * @param compId        идентификатор подборки
     * @param updateRequest DTO с обновленными данными подборки
     * @return DTO обновленной подборки
     * @throws ru.practicum.main.exception.NotFoundException если подборка или событие не найдены
     */
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    /**
     * Удаление подборки событий
     *
     * @param compId идентификатор подборки
     * @throws ru.practicum.main.exception.NotFoundException если подборка не найдена
     */
    void deleteCompilation(Long compId);

    /**
     * Получение списка подборок событий с фильтрацией по закрепленным
     *
     * @param pinned флаг закрепления подборки (опционально)
     * @param from   количество элементов для пропуска
     * @param size   количество элементов на странице
     * @return список DTO подборок
     */
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    /**
     * Получение подборки событий по идентификатору
     *
     * @param compId идентификатор подборки
     * @return DTO подборки
     * @throws ru.practicum.main.exception.NotFoundException если подборка не найдена
     */
    CompilationDto getCompilationById(Long compId);
}