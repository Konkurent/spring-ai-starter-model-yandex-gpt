package ru.ksoft.springaiyandexgpt.dto;

import lombok.Getter;
import org.springframework.ai.image.Image;
import reactor.core.publisher.Mono;

/**
 * Wraps {@link Image} for async generation: pixels arrive later via {@link Mono}; the Yandex Cloud
 * operation id can be used to poll status.
 */
public class AsyncImage extends Image {

    private final Mono<Image> asyncImage;

    /** Long-running operation identifier. */
    @Getter
    private final String operationId;

    /**
     * @param operationId operation id
     * @param asyncImage  publisher that completes with the final {@link Image} when generation finishes
     */
    public AsyncImage(String operationId, Mono<Image> asyncImage) {
        super(null, null);
        this.operationId = operationId;
        this.asyncImage = asyncImage;

    }

    /** Blocks until the base64 JSON payload is read from {@link #asyncImage}. */
    @Override
    public String getB64Json() {
        return asyncImage.map(Image::getB64Json).block();
    }

    /** Returns the reactive stream that completes with the final image. */
    public Mono<Image> asMono() {
        return asyncImage;
    }


}