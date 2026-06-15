package ru.kechlab.springaiyandexgpt.dto;

import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * HTTP client settings: request timeout and retry policy for blocking and reactive calls.
 */
public record ClientSpec(
        /** Timeout in the unit expected at the call site (often milliseconds). */
        Duration timeout,
        /** Exponential backoff settings and transient-error filter. */
        RetrySpec retrySpec
) {

    /**
     * Retry configuration: Spring {@link RetryTemplate} and Reactor {@link Retry} with exponential backoff.
     */
    public record RetrySpec(
            Integer maxAttempts,
            Double multiplier,
            Long minBackoff,
            Long maxBackoff
    ) {
        /** Retry policy for blocking code (Spring Retry). */
        public RetryTemplate retryTemplate() {
            RetryTemplate retryTemplate = new RetryTemplate();

            ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
            backOff.setInitialInterval(minBackoff);
            backOff.setMultiplier(multiplier);
            backOff.setMaxInterval(maxBackoff);

            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(maxAttempts));
            retryTemplate.setBackOffPolicy(backOff);
            

            return retryTemplate;
        }

        /** Retry policy for Reactor (streaming, etc.): 5xx responses and timeouts are treated as transient. */
        public Retry retry() {
            return Retry.backoff(maxAttempts, Duration.ofMillis(minBackoff))
                    .multiplier(multiplier)
                    .maxBackoff(Duration.ofMillis(maxBackoff))
                    .filter(this::isTransientError)
                    .onRetryExhaustedThrow((spec, signal) ->
                            new RuntimeException("Retries exhausted for streaming", signal.failure()));
        }

        private boolean isTransientError(Throwable throwable) {
            return throwable instanceof TimeoutException
                    || (throwable instanceof WebClientResponseException &&
                    ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
        }
    }

}
