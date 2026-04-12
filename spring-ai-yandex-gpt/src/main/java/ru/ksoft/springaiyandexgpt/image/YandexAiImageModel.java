package ru.ksoft.springaiyandexgpt.image;

import io.micrometer.observation.ObservationRegistry;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.image.*;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.ksoft.springaiyandexgpt.dto.AsyncImage;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.dto.ImageApi;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;
import ru.ksoft.springaiyandexgpt.dto.OperationApi.Operation;
import ru.ksoft.springaiyandexgpt.dto.OperationApi.OperationRequestSpec;
import ru.ksoft.springaiyandexgpt.dto.OperationApi.PathResolver;
import ru.ksoft.springaiyandexgpt.exceptions.YandexOperationFailedException;
import ru.ksoft.springaiyandexgpt.operation.OperationService;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatModel;
import ru.ksoft.springaiyandexgpt.utils.RetryExecutor;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Spring AI {@link ImageModel} for Yandex asynchronous image generation.
 * <p>
 * Starts generation via {@link YandexAiImageApi}, then polls the Yandex Cloud {@code Operation} API
 * until the image is ready. The returned {@link ru.ksoft.springaiyandexgpt.dto.AsyncImage} exposes a
 * {@link reactor.core.publisher.Mono} for non-blocking consumers and blocks in {@code getB64Json()}
 * for compatibility with code that expects a plain {@link org.springframework.ai.image.Image}.
 */
@Slf4j
public class YandexAiImageModel implements ImageModel {

    private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

    /**
     * The default options used for the image completion requests.
     */
    private final YandexAiImageOptions defaultOptions;

    /** Retries for blocking HTTP calls to the image generation endpoint. */
    private final RetryTemplate retryTemplate;

    /** HTTP client for starting image generation. */
    private final YandexAiImageApi imageApi;

    /**
     * Observation registry used for instrumentation.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Conventions to use for generating observations.
     */
    @Setter
    private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

    private final Scheduler pollingScheduler;

    private final OperationService<ImageApi.ImageResponseSpec> operationService;


    public YandexAiImageModel(
            YandexAiImageApi yandexAiImageApi,
            YandexAiImageOptions options,
            ClientSpec.RetrySpec retrySpec,
            ObservationRegistry observationRegistry,
            OperationService<ImageApi.ImageResponseSpec> operationService
    ) {
        Assert.notNull(operationService, "operationService must not be null");
        Assert.notNull(yandexAiImageApi, "OpenAiImageApi must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retrySpec, "retrySpec must not be null");
        Assert.notNull(observationRegistry, "observationRegistry must not be null");
        this.operationService = operationService;
        this.imageApi = yandexAiImageApi;
        this.defaultOptions = options;
        this.retryTemplate = retrySpec.retryTemplate();
        this.observationRegistry = observationRegistry;
        this.pollingScheduler = Schedulers.newBoundedElastic(10, 100, "imagePolling");
    }

    public YandexAiImageModel(
            YandexAiImageApi yandexAiImageApi,
            YandexAiImageOptions options,
            ClientSpec.RetrySpec retrySpec,
            ObservationRegistry observationRegistry,
            Scheduler pollingScheduler,
            OperationService<ImageApi.ImageResponseSpec> operationService
    ) {
        Assert.notNull(operationService, "operationService must not be null");
        Assert.notNull(yandexAiImageApi, "OpenAiImageApi must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retrySpec, "retrySpec must not be null");
        Assert.notNull(observationRegistry, "observationRegistry must not be null");
        Assert.notNull(pollingScheduler, "pullingScheduler must not be null");
        this.operationService = operationService;
        this.imageApi = yandexAiImageApi;
        this.defaultOptions = options;
        this.retryTemplate = retrySpec.retryTemplate();
        this.observationRegistry = observationRegistry;
        this.pollingScheduler = pollingScheduler;
    }

    @Override
    public @NonNull ImageResponse call(ImagePrompt prompt) {
        YandexAiImageOptions yaOptions = resolveOptions(prompt);
        ImageApi.YandexAiImageRequest imageRequest = createRequest(yaOptions, prompt.getInstructions());
        ImageModelObservationContext observationContext = ImageModelObservationContext.builder()
                .imagePrompt(new ImagePrompt(prompt.getInstructions(), yaOptions))
                .provider(YandexAiChatModel.PROVIDER_NAME)
                .build();

        return Objects.requireNonNull(ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    ResponseEntity<Operation<ImageApi.ImageResponseSpec>> imageResponseEntity = RetryExecutor
                            .execute(this.retryTemplate, (ctx) -> this.imageApi.generate(imageRequest, new HttpHeaders()));

                    ImageResponse imageResponse = convertResponse(imageResponseEntity, imageRequest);

                    observationContext.setResponse(imageResponse);

                    return imageResponse;
                }));
    }

    YandexAiImageOptions resolveOptions(ImagePrompt prompt) {
        return defaultOptions.merge(prompt.getOptions());
    }

    ImageApi.YandexAiImageRequest createRequest(YandexAiImageOptions yaOptions, List<ImageMessage> messages) {
        List<ImageApi.YandexAiImageRequest.Message> convertedMessage = new ArrayList<>(messages.size());
        messages.forEach(mess -> {
            if (mess.getWeight() != null) {
                log.debug("Parameter 'weight' is not supported yet");
            }
            convertedMessage.add(new ImageApi.YandexAiImageRequest.Message(mess.getText(), mess.getWeight()));
        });
        return ImageApi.YandexAiImageRequest.builder()
                .modelUri(yaOptions.getModel())
                .messages(convertedMessage)
                .generationOptions(new ImageApi.YandexAiImageRequest.GenerationOptions(yaOptions))
                .build();
    }

    ImageResponse convertResponse(ResponseEntity<Operation<ImageApi.ImageResponseSpec>> responseEntity, ImageApi.YandexAiImageRequest request) {
        Operation<ImageApi.ImageResponseSpec> response = responseEntity.getBody();

        Objects.requireNonNull(response, "response must not be null");

        Long created = response.createdAt().toInstant(ZoneOffset.UTC).toEpochMilli();

        ImageResponseMetadata imageResponseMetadata = new ImageResponseMetadata(created);
        responseEntity.getHeaders().forEach(imageResponseMetadata::put);

        List<ImageGeneration> imageGenerations = new ArrayList<>();
        imageGenerations.add(new ImageGeneration(new AsyncImage(response.id(), initPolling(response.id()))));

        return new ImageResponse(imageGenerations, imageResponseMetadata);
    }

    private Mono<Image> initPolling(String operationId) {
        OperationRequestSpec requestSpec = new OperationRequestSpec(operationId, PathResolver.Default.GET);
        return operationService.get(requestSpec)
                .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(1)))
                .takeUntil(OperationApi.Operation::done)
                .last()
                .timeout(Duration.ofSeconds(60))
                .subscribeOn(pollingScheduler)
                .flatMap(apiResponse -> {
                    if (apiResponse.responseSpec() != null) {
                        String base64 = apiResponse.responseSpec().image();
                        return Mono.just(new Image(null, base64));
                    } else {
                        return Mono.error(new YandexOperationFailedException(apiResponse.error()));
                    }
                });
    }

}
