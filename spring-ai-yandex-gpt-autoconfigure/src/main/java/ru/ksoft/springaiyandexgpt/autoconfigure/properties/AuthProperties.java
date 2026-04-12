package ru.ksoft.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Credentials under {@code spring.ai.auth.yandexai}: API key and/or IAM token (inline or file) with
 * optional refresh interval for file-based tokens.
 */
@Data
@ConfigurationProperties(prefix = AuthProperties.PREFIX)
public class AuthProperties {

    public static final String PREFIX = SpringAiYandexGptModelProperties.YANDEX_AI_AUTH;

    /** Secret API key for {@code Api-Key} authentication. */
    private String apiKey;

    @NestedConfigurationProperty
    private IamProperties iam;

    /** IAM token source and refresh schedule. */
    @Data
    public static class IamProperties {
        private String token;

        private Path tokenFile;

        private Duration refreshInterval = Duration.ofHours(1);
    }
}
