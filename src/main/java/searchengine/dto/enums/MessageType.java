package searchengine.dto.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    STOP_MESSAGE("Индексация остановлена пользователем"),
    ENTITY_MESSAGE(""),
    REFRESH_ERROR_MESSAGE("Данная страница находится за пределами сайтов, " +
            "\nуказанных в конфигурационном файле"),
    ENTITY_DB_MESSAGE("База данных пустая. Проведите индексацию сайтов."),
    START_ERROR_MESSAGE("Индексация уже запущена."),
    STOP_ERROR_MESSAGE("Индексация не запущена");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }
}
