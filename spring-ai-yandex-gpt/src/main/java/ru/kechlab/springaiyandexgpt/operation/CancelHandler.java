package ru.kechlab.springaiyandexgpt.operation;

import ru.kechlab.springaiyandexgpt.dto.OperationApi;

/**
 * Cancels a Yandex Cloud operation (for example when a client stops waiting for an async image).
 */
public interface CancelHandler {

    /** Cancels using the default {@code :cancel} path resolver. */
    default void cancel(String operationId) {
        cancel(operationId, OperationApi.PathResolver.Default.CANCEL);
    }

    void cancel(String operationId, OperationApi.PathResolver cancelPathResolver);

}
