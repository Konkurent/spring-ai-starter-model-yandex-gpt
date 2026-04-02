package ru.ksoft.springaiyandexgpt.text;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import ru.ksoft.springaiyandexgpt.api.HeaderProcessor;
import ru.ksoft.springaiyandexgpt.constants.CompletionResponseStatus;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ChatCompletionRequest;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ChatCompletionResponse;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ResultHolder;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class YandexChatApi {

    private final String baseUrl;
    private final String completionPath;

    private final List<HeaderProcessor> headerProcessors;
    private final RestClient restClient;
    private final WebClient webClient;

    private static final String REQUEST_BODY_NULL_MESSAGE = "The request body can not be null.";
    private static final String STREAM_FALSE_MESSAGE = "Request must set the stream property to false.";

    public YandexChatApi(String baseUrl, String completionPath, List<HeaderProcessor> headerProcessors,  RestClient.Builder restClientBuilder,
                         WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {
        Assert.hasText(baseUrl, "Base url must not be null");
        Assert.hasText(completionPath, "Completions Path must not be null");
        this.baseUrl = baseUrl;
        this.completionPath = completionPath;

        this.headerProcessors = headerProcessors;

        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .defaultStatusHandler(responseErrorHandler)
                .build();

        this.webClient = webClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    public ResponseEntity<ResultHolder<ChatCompletionResponse>> completionEntity(ChatCompletionRequest request) {
        return completionEntity(request, new HttpHeaders());
    }

    public ResponseEntity<ResultHolder<ChatCompletionResponse>> completionEntity(ChatCompletionRequest request, HttpHeaders headers) {
        Assert.notNull(request, REQUEST_BODY_NULL_MESSAGE);
        Assert.isTrue(!request.completionOptions().stream(), STREAM_FALSE_MESSAGE);
        headerProcessors.forEach(headerProcessor -> headerProcessor.process(headers));
        return restClient.post()
                .uri(completionPath)
                .headers((httpHeaders) -> httpHeaders.addAll(headers))
                .body(request)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

    public Flux<ChatCompletionResponse> completionStream(ChatCompletionRequest request) {
        return  completionStream(request, new HttpHeaders());
    }

    public Flux<ChatCompletionResponse> completionStream(ChatCompletionRequest request, HttpHeaders headers) {
        Assert.notNull(request, REQUEST_BODY_NULL_MESSAGE);
        Assert.isTrue(request.completionOptions().stream(), "Request must set the stream property to true.");
        headerProcessors.forEach(headerProcessor -> headerProcessor.process(headers));
        return webClient.post()
                .uri(this.completionPath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatCompletionResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isTransientError)
                        .onRetryExhaustedThrow((spec, signal) ->
                                new RuntimeException("Retries exhausted for streaming", signal.failure())))
                .takeUntil(
                        response -> response.alternatives().stream()
                                .anyMatch(alternative -> alternative.status() == CompletionResponseStatus.ALTERNATIVE_STATUS_FINAL)
                );

    }

    private boolean isTransientError(Throwable throwable) {
        return throwable instanceof TimeoutException
                || (throwable instanceof WebClientResponseException &&
                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
    }


}
