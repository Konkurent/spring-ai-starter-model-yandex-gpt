package ru.ksoft.springaiyandexgpt.utils;

import lombok.experimental.UtilityClass;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryException;
import org.springframework.retry.support.RetryTemplate;

@UtilityClass
public class RetryExecutor {
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
