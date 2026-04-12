package ru.ksoft.springaiyandexgpt.operation;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.ksoft.springaiyandexgpt.api.HeadersProcessor;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;

import java.util.List;

/**
 * HTTP client for Yandex Cloud long-running {@link ru.ksoft.springaiyandexgpt.dto.OperationApi.Operation}
 * resources: reactive {@code GET} status and blocking cancel-style calls.
 * <p>
 * Request paths are built from a configurable base URL, operation sub-path, and
 * {@link ru.ksoft.springaiyandexgpt.dto.OperationApi.PathResolver}.
 */
public class OperationClient {


    private final String operationSubPath;

    private final List<HeadersProcessor> headersProcessors;

    private final WebClient webClient;

    private final RestClient restClient;

    public OperationClient(String baseUrl,
                           String operationSubPath,
                           List<HeadersProcessor> headersProcessors,
                           WebClient.Builder webClientBuilder,
                           RestClient.Builder restClientBuilder
    ) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        Assert.hasText(operationSubPath, "operationSubPath must not be empty");
        Assert.notNull(headersProcessors, "headerProcessors must not be null");
        Assert.notNull(webClientBuilder, "webClientBuilder must not be null");
        Assert.notNull(restClientBuilder, "restClientBuilder must not be null");
        this.headersProcessors = headersProcessors;
        this.operationSubPath = operationSubPath;
        this.webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    public OperationClient(String baseUrl,
                           String operationSubPath,
                           List<HeadersProcessor> headersProcessors,
                           WebClient.Builder webClientBuilder,
                           RestClient.Builder restClientBuilder,
                           Scheduler scheduler
    ) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        Assert.hasText(operationSubPath, "operationSubPath must not be empty");
        Assert.notNull(headersProcessors, "headerProcessors must not be null");
        Assert.notNull(webClientBuilder, "webClientBuilder must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.notNull(restClientBuilder, "restClientBuilder must not be null");
        this.headersProcessors = headersProcessors;
        this.operationSubPath = operationSubPath;
        this.webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    public <T> Mono<OperationApi.Operation<T>> get(OperationApi.OperationRequestSpec requestSpec) {
        headersProcessors.forEach(processor -> processor.process(requestSpec.headers()));
        return webClient.post()
                .uri("/" + String.join("/", operationSubPath, requestSpec.pathResolver().resolve(requestSpec.id())))
                .headers(headers -> headers.addAll(requestSpec.headers()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public <T> ResponseEntity<OperationApi.Operation<T>> cancel(OperationApi.OperationRequestSpec requestSpec) {
        return restClient.post()
                .uri("/" + String.join("/", operationSubPath, requestSpec.pathResolver().resolve(requestSpec.id())))
                .headers(headers -> headers.addAll(requestSpec.headers()))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

}
