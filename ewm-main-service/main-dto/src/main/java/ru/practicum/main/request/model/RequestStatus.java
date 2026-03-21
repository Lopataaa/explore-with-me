package ru.practicum.main.request.model;

public enum RequestStatus {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтверждено"),
    REJECTED("Отклонено"),
    CANCELED("Отменено");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}