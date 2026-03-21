package ru.practicum.main.event.model;

public enum EventState {
    PENDING("Ожидает публикации"),
    PUBLISHED("Опубликовано"),
    CANCELED("Отменено");

    private final String description;

    EventState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}