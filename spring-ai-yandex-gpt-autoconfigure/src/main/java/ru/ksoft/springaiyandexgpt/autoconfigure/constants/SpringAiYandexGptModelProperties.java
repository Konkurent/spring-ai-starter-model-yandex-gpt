package ru.ksoft.springaiyandexgpt.autoconfigure.constants;

import lombok.experimental.UtilityClass;
import org.springframework.ai.model.SpringAIModelProperties;

/**
 * Central string constants for Spring Boot property keys: model provider id {@code yandexai} and
 * derived prefixes for auth, chat, embedding, and image configuration.
 */
@UtilityClass
public class SpringAiYandexGptModelProperties {

    public static final String MODEL_PREFIX = SpringAIModelProperties.MODEL_PREFIX;

    public static final String AUTH = "spring.ai.auth";

    public static final String CHAT_MODEL = SpringAIModelProperties.CHAT_MODEL;

    public static final String EMBEDDING_MODEL = SpringAIModelProperties.EMBEDDING_MODEL;

    public static final String IMAGE_MODEL = SpringAIModelProperties.IMAGE_MODEL;

    public static final String YANDEX_AI = "yandexai";

    public static final String YANDEX_AI_AUTH =  AUTH + "." + YANDEX_AI;

    public static final String YANDEX_AI_CHAT_MODEL = CHAT_MODEL + "." + YANDEX_AI;

    public static final String YANDEX_AI_EMBEDDING_MODEL = EMBEDDING_MODEL + "." + YANDEX_AI;

    public static final String YANDEX_AI_IMAGE_MODEL = IMAGE_MODEL + "." + YANDEX_AI;

}
