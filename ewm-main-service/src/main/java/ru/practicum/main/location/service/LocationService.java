package ru.practicum.main.location.service;

import ru.practicum.main.location.dto.CreateLocationRequest;
import ru.practicum.main.location.dto.LocationDto;

import java.util.List;

/**
 * Сервис для управления локациями
 */
public interface LocationService {

    /**
     * Создание новой локации (только для администратора)
     *
     * @param adminId ID администратора, создающего локацию
     * @param request данные для создания локации
     * @return DTO созданной локации
     * @throws ru.practicum.main.exception.ConflictException если локация с таким названием уже существует
     * @throws ru.practicum.main.exception.BadRequestException если координаты некорректны
     */
    LocationDto createLocation(Long adminId, CreateLocationRequest request);

    /**
     * Обновление существующей локации (только для администратора)
     *
     * @param adminId    ID администратора
     * @param locationId ID обновляемой локации
     * @param request    новые данные для локации
     * @return DTO обновленной локации
     * @throws ru.practicum.main.exception.NotFoundException если локация не найдена
     * @throws ru.practicum.main.exception.ConflictException если новое название уже занято
     */
    LocationDto updateLocation(Long adminId, Long locationId, CreateLocationRequest request);

    /**
     * Удаление локации (только для администратора)
     *
     * @param adminId    ID администратора
     * @param locationId ID удаляемой локации
     * @throws ru.practicum.main.exception.NotFoundException если локация не найдена
     * @throws ru.practicum.main.exception.ConflictException если локация используется событиями
     */
    void deleteLocation(Long adminId, Long locationId);

    /**
     * Получение локации по ID (публичный доступ)
     *
     * @param id ID локации
     * @return DTO локации
     * @throws ru.practicum.main.exception.NotFoundException если локация не найдена
     */
    LocationDto getLocationById(Long id);

    /**
     * Получение списка всех локаций с пагинацией (публичный доступ)
     *
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список DTO локаций
     */
    List<LocationDto> getAllLocations(Integer from, Integer size);
}