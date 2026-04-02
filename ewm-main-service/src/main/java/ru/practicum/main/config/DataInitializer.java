package ru.practicum.main.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("=== INITIALIZING TEST DATA ===");

        try {
            Category category;
            if (categoryRepository.count() == 0) {
                category = categoryRepository.save(
                        Category.builder().name("Концерты").build()
                );
                log.info("Created category: id={}", category.getId());
            } else {
                category = categoryRepository.findAll().get(0);
                log.info("Category already exists: id={}", category.getId());
            }

            User user;
            if (userRepository.count() == 0) {
                user = userRepository.save(
                        User.builder()
                                .name("Test User")
                                .email("test@example.com")
                                .build()
                );
                log.info("Created user: id={}", user.getId());
            } else {
                user = userRepository.findAll().get(0);
                log.info("User already exists: id={}", user.getId());
            }

            if (eventRepository.count() == 0) {
                Location location = Location.builder()
                        .lat(55.754167f)
                        .lon(37.62f)
                        .build();

                Event event = Event.builder()
                        .annotation("Тестовое событие для проверки API")
                        .category(category)
                        .description("Полное описание тестового события")
                        .eventDate(LocalDateTime.now().plusDays(30))
                        .initiator(user)
                        .location(location)
                        .paid(false)
                        .participantLimit(10)
                        .requestModeration(true)
                        .title("Тестовое событие")
                        .createdOn(LocalDateTime.now())
                        .publishedOn(LocalDateTime.now())
                        .state(EventState.PUBLISHED)
                        .views(0L)
                        .build();

                eventRepository.save(event);
                log.info("Created published event");
            } else {
                log.info("Events already exist, count: {}", eventRepository.count());
            }

        } catch (Exception e) {
            log.error("Error creating test data: {}", e.getMessage());
        }

        log.info("=== TEST DATA INITIALIZATION COMPLETE ===");
    }
}