package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

import java.util.List;

/**
 * Сервис для работы с категориями событий
 */
public interface CategoryService {

    /**
     * Создание новой категории
     *
     * @param newCategoryDto данные для создания категории
     * @return DTO созданной категории
     * @throws ru.practicum.main.exception.ConflictException если категория с таким именем уже существует
     */
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    /**
     * Получение списка категорий с пагинацией
     *
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список DTO категорий
     */
    List<CategoryDto> getCategories(Integer from, Integer size);

    /**
     * Получение категории по идентификатору
     *
     * @param catId идентификатор категории
     * @return DTO категории
     * @throws ru.practicum.main.exception.NotFoundException если категория не найдена
     */
    CategoryDto getCategoryById(Long catId);

    /**
     * Обновление категории
     *
     * @param catId       идентификатор категории
     * @param categoryDto данные для обновления
     * @return DTO обновленной категории
     * @throws ru.practicum.main.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.main.exception.ConflictException если новое имя уже занято
     */
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    /**
     * Удаление категории
     *
     * @param catId идентификатор категории
     * @throws ru.practicum.main.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.main.exception.ConflictException если к категории привязаны события
     */
    void deleteCategory(Long catId);
}