package ru.kechlab.springaiyandexgpt.embeddings;

import com.fasterxml.jackson.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import ru.kechlab.springaiyandexgpt.api.HttpHeadersProcessor;

import java.util.List;

/**
 * Blocking REST client for the Yandex Foundation Models embeddings endpoint.
 * <p>
 * Request and response bodies match the JSON fields expected by the service ({@code modelUri},
 * {@code text}, {@code dim}, etc.).
 */
public class YandexAiEmbeddingApi {
    private final String baseUrl;
    private final String embeddingPath;

    private final List<HttpHeadersProcessor> headersProcessors;
    private final RestClient restClient;

    public YandexAiEmbeddingApi(String baseUrl, String embeddingPath,
                                List<HttpHeadersProcessor> headersProcessors,
                                RestClient.Builder restClientBuilder) {
        Assert.hasText(baseUrl, "Base url must not be null");
        this.baseUrl = baseUrl;
        this.embeddingPath = embeddingPath;
        this.headersProcessors = headersProcessors;
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    public ResponseEntity<EmbeddingResponse> embedding(EmbeddingRequest embeddingRequest, HttpHeaders headers) {
        Assert.notNull(embeddingRequest.modelUri, "Embedding request modelUri must not be null");
        Assert.notNull(embeddingRequest.text, "Embedding request text must not be null");
        headersProcessors.forEach(processor -> processor.process(headers));
        return restClient.post()
                .uri(embeddingPath)
                .body(embeddingRequest)
                .retrieve()
                .toEntity(EmbeddingResponse.class);
    }


    @JsonPropertyOrder(
            {
                    "modelUri",
                    "text",
                    "dim"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmbeddingRequest(
            @JsonProperty("modelUri") String modelUri,
            @JsonProperty("text") String text,
            @JsonProperty("dim") String dim
    ) { }

    @JsonPropertyOrder(
            {
                    "embedding",
                    "numTokens",
                    "modelVersion"
            }
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingResponse(
            @JsonProperty("embedding") float[] embedding,
            @JsonProperty("numTokens") String numTokens,
            @JsonProperty("modelVersion") String modelVersion
    ) {}


}
