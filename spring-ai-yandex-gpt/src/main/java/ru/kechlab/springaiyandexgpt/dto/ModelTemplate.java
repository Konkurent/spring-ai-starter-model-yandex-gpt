package ru.kechlab.springaiyandexgpt.dto;

import org.springframework.ai.model.ModelDescription;

/**
 * Yandex AI model description: display name and resource URI construction (e.g. folder id substitution).
 */
public interface ModelTemplate {

    /** API model URI; arguments depend on the implementation (e.g. folder id). */
    String formatUri(String... args);

}
