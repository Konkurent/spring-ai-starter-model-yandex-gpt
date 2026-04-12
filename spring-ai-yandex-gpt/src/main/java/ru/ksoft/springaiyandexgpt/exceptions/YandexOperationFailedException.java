package ru.ksoft.springaiyandexgpt.exceptions;

import ru.ksoft.springaiyandexgpt.constants.ErrorCode;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;

import java.util.Map;

/**
 * Thrown when a Yandex Cloud operation completes with an error payload instead of a successful
 * {@code response} (for example failed image generation).
 */
public class YandexOperationFailedException extends RuntimeException {

    private final ErrorCode errorCode;

    private final Map<String, Object> details;

    /**
     * @param error operation error body from the Yandex API; must not be {@code null}
     */
    public YandexOperationFailedException(OperationApi.Operation.Error error) {
        super(error.message());
        errorCode = error.code();
        details = error.details();
    }

}
