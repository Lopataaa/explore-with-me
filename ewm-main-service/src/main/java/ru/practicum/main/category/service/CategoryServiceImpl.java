package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с категориями событий
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Создание новой категории
     *
     * @param newCategoryDto данные для создания категории
     * @return DTO созданной категории
     * @throws ConflictException если категория с таким именем уже существует
     */
    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding category: {}", newCategoryDto);

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category with name " + newCategoryDto.getName() + " already exists");
        }

        Category category = categoryMapper.toCategory(newCategoryDto);
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryDto(category);
    }

    /**
     * Получение списка категорий с пагинацией
     *
     * @param from количество элементов для пропуска
     * @param size количество элементов на странице
     * @return список DTO категорий
     */
    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Getting categories with from: {}, size: {}", from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).getContent().stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение категории по идентификатору
     *
     * @param catId идентификатор категории
     * @return DTO категории
     * @throws NotFoundException если категория не найдена
     */
    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Getting category by id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        return categoryMapper.toCategoryDto(category);
    }

    /**
     * Обновление категории
     *
     * @param catId       идентификатор категории
     * @param categoryDto данные для обновления
     * @return DTO обновленной категории
     * @throws NotFoundException если категория не найдена
     * @throws ConflictException если новое имя уже занято
     */
    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with id: {}, name: {}", catId, categoryDto.getName());

        if (categoryDto.getName() == null || categoryDto.getName().isBlank()) {
            throw new BadRequestException("Name must not be blank");
        }
        if (categoryDto.getName().length() > 50) {
            throw new BadRequestException("Name length must be no more than 50");
        }

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (!category.getName().equals(categoryDto.getName()) && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Category with name " + categoryDto.getName() + " already exists");
        }

        category.setName(categoryDto.getName());
        category = categoryRepository.save(category);

        return categoryMapper.toCategoryDto(category);
    }

    /**
     * Удаление категории
     *
     * @param catId идентификатор категории
     * @throws NotFoundException если категория не найдена
     * @throws ConflictException если к категории привязаны события
     */
    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.delete(category);
    }
}