package ru.kechlab.springaiyandexgpt.operation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import ru.kechlab.springaiyandexgpt.dto.ClientSpec;
import ru.kechlab.springaiyandexgpt.dto.OperationApi;
import ru.kechlab.springaiyandexgpt.utils.RetryExecutor;

/**
 * High-level helper around {@link OperationClient}: retries reactive polling and blocking cancel,
 * and triggers {@link CancelHandler} when a reactive subscription is cancelled.
 *
 * @param <T> type of the completed operation {@code response} payload
 */
@Slf4j
public class OperationService<T> {

    private final RetryTemplate retryTemplate;

    private final Retry retry;

    private final CancelHandler cancelHandler;

    private final OperationClient client;

    public OperationService(ClientSpec.RetrySpec retrySpec, OperationClient client, CancelHandler cancelHandler) {
        Assert.notNull(retrySpec, "retrySpec must not be null");
        Assert.notNull(cancelHandler, "cancelHandler must not be null");
        Assert.notNull(client, "client must not be null");
        this.retryTemplate = retrySpec.retryTemplate();
        this.retry = retrySpec.retry();
        this.cancelHandler = cancelHandler;
        this.client = client;
    }


    public Mono<OperationApi.Operation<T>> get(OperationApi.OperationRequestSpec requestSpec) {
        return client.<T>get(requestSpec)
                .retryWhen(retry)
                .doOnCancel(() -> Mono.fromRunnable(
                        () -> cancelHandler.cancel(requestSpec.id()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe()
                );
    }

    public ResponseEntity<OperationApi.Operation<T>> cancel(OperationApi.OperationRequestSpec requestSpec) {
        return RetryExecutor.execute(retryTemplate, ctx -> client.cancel(requestSpec));
    }



}
