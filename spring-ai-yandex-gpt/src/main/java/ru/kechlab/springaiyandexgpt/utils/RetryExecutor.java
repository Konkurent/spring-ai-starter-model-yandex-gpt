package ru.kechlab.springaiyandexgpt.utils;

import lombok.experimental.UtilityClass;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryException;
import org.springframework.retry.support.RetryTemplate;

/**
 * Runs a {@link RetryCallback} with {@link RetryTemplate} and unwraps {@link RetryException} into
 * a normal runtime exception for callers.
 */
@UtilityClass
public class RetryExecutor {

    /**
     * @param retryTemplate Spring Retry template (backoff, max attempts, etc.)
     * @param retryable     work to run with automatic retries
     * @param <R>           result type
     * @return value returned by the callback
     */
    public static <R> R execute(RetryTemplate retryTemplate, RetryCallback<R, RetryException> retryable) {
        try {
            return retryTemplate.execute(retryable);
        }
        catch (RetryException e) {
            throw (e.getCause() instanceof RuntimeException runtime) ? runtime
                    : new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
