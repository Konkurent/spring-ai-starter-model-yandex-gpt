package ru.ksoft.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.constants.CompletionModel;
import ru.ksoft.springaiyandexgpt.constants.ReasoningMode;

/**
 * Chat model settings under {@code spring.ai.model.chat.yandexai}: completion path, default
 * {@link ru.ksoft.springaiyandexgpt.constants.CompletionModel}, generation options, optional folder
 * override, and optional client overrides.
 */
@Data
@ConfigurationProperties(prefix = ChatProperties.PREFIX)
public class ChatProperties {

    public static final String PREFIX = SpringAiYandexGptModelProperties.YANDEX_AI_CHAT_MODEL;

    /** Default REST path for chat completions (relative to the shared base URL). */
    public static final String COMPLETION_PATH = "/foundationModels/v1/completions";

    /** Overrides {@link ru.ksoft.springaiyandexgpt.autoconfigure.properties.YandexAiProperties#getFolderId()} for chat only when set. */
    private String folderId;

    private String completionPath = COMPLETION_PATH;

    private CompletionModel model = CompletionModel.YANDEX_GPT_LITE_5;

    @NestedConfigurationProperty
    private Options options = new Options();

    @NestedConfigurationProperty
    private ClientProperties client;

    @Data
    public static class Options {
        private Float temperature = 0.7F;
        private Integer maxTokens;
        private ReasoningMode reasoningMode = ReasoningMode.DISABLED;
    }
}
