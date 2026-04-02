package ru.ksoft.springaiyandexgpt.dto;

import org.springframework.ai.model.ModelDescription;

public interface Model extends ModelDescription {

    String getUri(String... args);

}
