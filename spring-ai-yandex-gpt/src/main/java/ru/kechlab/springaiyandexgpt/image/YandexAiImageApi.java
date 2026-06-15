package ru.kechlab.springaiyandexgpt.image;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import ru.kechlab.springaiyandexgpt.api.HttpHeadersProcessor;
import ru.kechlab.springaiyandexgpt.dto.ImageApi;
import ru.kechlab.springaiyandexgpt.dto.ImageApi.YandexAiImageRequest;
import ru.kechlab.springaiyandexgpt.dto.OperationApi.Operation;

import java.util.List;

/**
 * Blocking REST client that submits an image generation request and returns a Yandex Cloud
 * {@link ru.kechlab.springaiyandexgpt.dto.OperationApi.Operation} (long-running job).
 */
public class YandexAiImageApi {

    private final String imagePath;

    private final List<HttpHeadersProcessor> headersProcessors;

    private final RestClient restClient;

    public YandexAiImageApi(String baseUrl, String imagePath, List<HttpHeadersProcessor> headersProcessors, RestClient.Builder restBuilder) {
        Assert.notNull(baseUrl, "baseUrl must not be null");
        Assert.notNull(imagePath, "imagePath must not be null");
        Assert.notNull(restBuilder, "restBuilder must not be null");

        this.imagePath = imagePath;
        this.headersProcessors = headersProcessors;
        this.restClient = restBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public ResponseEntity<Operation<ImageApi.ImageResponseSpec>> generate(YandexAiImageRequest request, HttpHeaders headers) {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(headers, "headers must not be null");
        for (HttpHeadersProcessor processor : headersProcessors) {
            processor.process(headers);
        }
        return restClient.post()
                .uri(imagePath)
                .body(request)
                .headers((httpHeaders) -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

    }

}
