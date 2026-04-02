package ru.ksoft.springaiyandexgpt.embedings;

import com.fasterxml.jackson.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import ru.ksoft.springaiyandexgpt.api.HeaderProcessor;

import java.util.List;

public class EmbeddingApi {
    private final String baseUrl;
    private final String embeddingPath;

    private final List<HeaderProcessor> headerProcessors;
    private final RestClient restClient;

    public EmbeddingApi(String baseUrl, String embeddingPath, List<HeaderProcessor> headerProcessors, RestClient.Builder restClientBuilder) {
        Assert.hasText(baseUrl, "Base url must not be null");
        this.baseUrl = baseUrl;
        this.embeddingPath = embeddingPath;
        this.headerProcessors = headerProcessors;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public ResponseEntity<EmbeddingResponse> embedding(EmbeddingRequest embeddingRequest, HttpHeaders headers) {
        Assert.notNull(embeddingRequest.modelUri, "Embedding request modelUri must not be null");
        Assert.notNull(embeddingRequest.text, "Embedding request text must not be null");
        headerProcessors.forEach(processor -> processor.process(headers));
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
