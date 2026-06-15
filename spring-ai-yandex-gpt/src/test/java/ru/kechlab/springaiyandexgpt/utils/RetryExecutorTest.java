package ru.kechlab.springaiyandexgpt.utils;

import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryException;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryExecutorTest {

    @Test
    void executeReturnsValueOnSuccess() {
        RetryTemplate template = singleAttemptTemplate();
        String result = RetryExecutor.execute(template, ctx -> "ok");
        assertThat(result).isEqualTo("ok");
    }

    @Test
    void executePropagatesRuntimeCauseAfterRetries() {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(2));

        assertThatThrownBy(() -> RetryExecutor.execute(template, ctx -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class).hasMessage("boom");
    }

    @Test
    void executeWrapsNonRuntimeCauseFromRetryException() {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(2));

        assertThatThrownBy(() -> RetryExecutor.execute(template, ctx -> {
            throw new RetryException("x", new java.io.IOException("io"));
        })).hasCauseInstanceOf(java.io.IOException.class);
    }

    private static RetryTemplate singleAttemptTemplate() {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new SimpleRetryPolicy(1));
        return template;
    }
}
