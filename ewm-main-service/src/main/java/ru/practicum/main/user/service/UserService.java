package ru.practicum.main.user.service;

import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;

import java.util.List;

/**
 * Сервис для работы с пользователями
 */
public interface UserService {

    /**
     * Регистрация нового пользователя
     *
     * @param request данные для регистрации пользователя
     * @return DTO созданного пользователя
     * @throws ru.practicum.main.exception.ConflictException если пользователь с таким email уже существует
     */
    UserDto registerUser(NewUserRequest request);

    /**
     * Получение списка пользователей с пагинацией
     *
     * @param ids  список идентификаторов пользователей для фильтрации (опционально)
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список DTO пользователей
     */
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    /**
     * Удаление пользователя
     *
     * @param userId идентификатор пользователя
     * @throws ru.practicum.main.exception.NotFoundException если пользователь не найден
     */
    void deleteUser(Long userId);
}