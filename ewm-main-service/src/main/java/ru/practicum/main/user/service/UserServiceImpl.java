package ru.practicum.main.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.mapper.UserMapper;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с пользователями
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Регистрация нового пользователя
     *
     * @param newUserRequest данные для регистрации
     * @return DTO зарегистрированного пользователя
     * @throws ConflictException если пользователь с таким email уже существует
     */
    @Override
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Registering user: {}", newUserRequest);

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("User with email " + newUserRequest.getEmail() + " already exists");
        }

        User user = userMapper.toUser(newUserRequest);
        user = userRepository.save(user);

        return userMapper.toUserDto(user);
    }

    /**
     * Получение списка пользователей
     *
     * @param ids  список идентификаторов пользователей (если null - все пользователи)
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список DTO пользователей
     */
    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Getting users with ids: {}, from: {}, size: {}", ids, from, size);

        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return userRepository.findAll(pageable).getContent().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Удаление пользователя
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        userRepository.delete(user);
    }
}