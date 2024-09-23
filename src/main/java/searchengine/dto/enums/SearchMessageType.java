package searchengine.dto.enums;

import lombok.Getter;

@Getter
public enum SearchMessageType {
    STOP_MESSAGE(""),
    ENTITY_QUERY_MESSAGE("Задан пустой поисковый запрос"),
    NULL_ERROR_MESSAGE("Сайт с этим путем не индексируется в нашем API. " +
            "Попробуйте другой путь."),
    ENTITY_DB_MESSAGE("Поиск невозможен. База данных пустая."),
    FAILED_ERROR_MESSAGE("Поиск невозможен. Не выполнена индексация сайтов."),
    INDEX_ERROR_MESSAGE("Поиск невозможен. Идет индексация.");

    private final String description;

    SearchMessageType(String description) {
        this.description = description;
    }
}
