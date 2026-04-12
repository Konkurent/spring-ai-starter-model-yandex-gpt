package ru.ksoft.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;

/**
 * Shared settings under {@code spring.ai.model}: Yandex Cloud folder id, Foundation Models base URL,
 * operations API path segment, and default HTTP client timeouts/retries.
 */
@Data
@ConfigurationProperties(prefix = YandexAiProperties.PREFIX)
public class YandexAiProperties {

    public static final String PREFIX = SpringAiYandexGptModelProperties.MODEL_PREFIX;

    /** Default HTTPS endpoint for Foundation Models. */
    public static final String BASE_URL = "https://llm.api.cloud.yandex.net/foundationModels";

    /** Default relative path for the operations API (concatenated with base URL by the client). */
    public static final String OPERATION_PATH = "/operations";

    /** Yandex Cloud folder id used when a feature-specific folder property is not set. */
    private String folderId;

    private String baseUrl = BASE_URL;

    private String operationPath = "operations";

    @NestedConfigurationProperty
    private ClientProperties client = new ClientProperties();

}
