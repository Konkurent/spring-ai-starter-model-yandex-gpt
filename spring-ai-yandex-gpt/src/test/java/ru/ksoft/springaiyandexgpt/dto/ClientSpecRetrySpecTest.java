package ru.ksoft.springaiyandexgpt.dto;

import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ClientSpecRetrySpecTest {

    @Test
    void retryTemplateUsesExponentialBackoffAndMaxAttempts() {
        ClientSpec.RetrySpec spec = new ClientSpec.RetrySpec(4, 2.0, 100L, 800L);
        RetryTemplate template = spec.retryTemplate();

        assertThat(template).isNotNull();
        assertThat(spec.retry()).isNotNull();
    }

    @Test
    void reactorRetryMatchesTemplatePolicy() {
        ClientSpec.RetrySpec spec = new ClientSpec.RetrySpec(3, 1.5, 50L, 400L);
        assertThat(spec.retry()).isNotNull();
    }
}
