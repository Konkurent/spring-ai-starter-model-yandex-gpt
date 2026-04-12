package ru.ksoft.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * HTTP client tuning: request timeout and retry/backoff parameters used to build {@link ru.ksoft.springaiyandexgpt.dto.ClientSpec}.
 */
@Data
public class ClientProperties {

    /** Overall request timeout; interpretation depends on how the Rest/Web client is configured. */
    private Duration timeout;

    @NestedConfigurationProperty
    private RetryProperties retry = new RetryProperties();

    /** Retry counts and exponential backoff intervals (milliseconds). */
    @Data
    public static class RetryProperties {
        private Integer maxAttempts = 1;
        private Double multiplier = 2.0D;
        private Long minBackoff = 1000L;
        private Long maxBackoff = 60_0000L;
    }

}
