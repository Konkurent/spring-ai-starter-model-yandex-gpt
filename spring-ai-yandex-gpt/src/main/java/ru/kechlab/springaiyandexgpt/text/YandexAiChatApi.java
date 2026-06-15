package ru.kechlab.springaiyandexgpt.text;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.kechlab.springaiyandexgpt.api.HttpHeadersProcessor;
import ru.kechlab.springaiyandexgpt.dto.ChatApi.ChatCompletionRequest;
import ru.kechlab.springaiyandexgpt.dto.ChatApi.ChatCompletionResponse;
import ru.kechlab.springaiyandexgpt.dto.ChatApi.ResultHolder;

import java.util.List;

public class YandexAiChatApi {

    private final String completionPath;

    private final List<HttpHeadersProcessor> headersProcessors;

    private final RestClient restClient;
    private final WebClient webClient;

    private static final String REQUEST_BODY_NULL_MESSAGE = "The request body can not be null.";
    private static final String STREAM_FALSE_MESSAGE = "Request must set the stream property to false.";

    public YandexAiChatApi(
            String baseUrl, String completionPath,
            List<HttpHeadersProcessor> headersProcessors,
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder
    ) {
        Assert.hasText(baseUrl, "Base url must not be null");
        Assert.hasText(completionPath, "Completions Path must not be null");
        Assert.notNull(headersProcessors, "Headers processors must not be null");
        Assert.notNull(restClientBuilder, "Rest client builder must not be null");
        Assert.notNull(webClientBuilder, "Web client builder must not be null");
        this.completionPath = completionPath;
        this.headersProcessors = headersProcessors;
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
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
        headersProcessors.forEach(headerProcessor -> headerProcessor.process(headers));
        return restClient.post()
                .uri(completionPath)
                .headers((httpHeaders) -> httpHeaders.addAll(headers))
                .body(request)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

    public Flux<ChatCompletionResponse> completionStream(ChatCompletionRequest request) {
        return completionStream(request, new HttpHeaders());
    }

    public Flux<ChatCompletionResponse> completionStream(ChatCompletionRequest request, HttpHeaders headers) {
        Assert.notNull(request, REQUEST_BODY_NULL_MESSAGE);
        Assert.isTrue(request.completionOptions().stream(), "Request must set the stream property to true.");
        headersProcessors.forEach(headerProcessor -> headerProcessor.process(headers));
        return webClient.post()
                .uri(this.completionPath)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatCompletionResponse.class);

    }

}
