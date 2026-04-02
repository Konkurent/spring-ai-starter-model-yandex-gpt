package ru.ksoft.springaiyandexgpt.embedings;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import ru.ksoft.springaiyandexgpt.constants.EmbeddingModel;
import ru.ksoft.springaiyandexgpt.dto.EmbeddingApi.EmbeddingRequestSpec;
import ru.ksoft.springaiyandexgpt.text.YandexChatModel;
import ru.ksoft.springaiyandexgpt.utils.RetryExecutor;

import java.util.List;


@Slf4j
public class YandexAiEmbeddingModel extends AbstractEmbeddingModel {

    private static final String DUMMY_TEXT = "Тестовое сообщение для получения размерности эмбеддинга";

    private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

    private final YandexGptEmbeddingOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    private final ObservationRegistry observationRegistry;

    private final EmbeddingModelObservationConvention observationConvention;

    private final EmbeddingApi embeddingApi;

    private final MetadataMode metadataMode;

    public YandexAiEmbeddingModel(EmbeddingApi embeddingApi, MetadataMode metadataMode, YandexGptEmbeddingOptions options,
                                  org.springframework.retry.support.RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
        this(embeddingApi, metadataMode, options, retryTemplate, observationRegistry, DEFAULT_OBSERVATION_CONVENTION);
    }

    public YandexAiEmbeddingModel(EmbeddingApi embeddingApi, MetadataMode metadataMode, YandexGptEmbeddingOptions options,
                                  org.springframework.retry.support.RetryTemplate retryTemplate, ObservationRegistry observationRegistry, EmbeddingModelObservationConvention observationConvention) {
        Assert.notNull(embeddingApi, "embeddingApi must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        Assert.notNull(observationRegistry, "observationRegistry must not be null");

        this.embeddingApi = embeddingApi;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }


    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return call(request, EmbeddingModel.DOC);
    }

    public EmbeddingResponse call(EmbeddingRequest request, EmbeddingModel model) {
        var observationContext = EmbeddingModelObservationContext.builder()
                .embeddingRequest(request)
                .provider(YandexChatModel.PROVIDER_NAME)
                .build();

        EmbeddingRequestSpec requestSpec = buildRequest(request, model);
        EmbeddingApi.EmbeddingRequest internalRequest = new EmbeddingApi.EmbeddingRequest(
                requestSpec.modelUri(),
                requestSpec.text(),
                requestSpec.dim()
        );

        return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    ResponseEntity<EmbeddingApi.EmbeddingResponse> apiEmbeddingResponse = RetryExecutor.execute(this.retryTemplate,
                            (ctx) -> embeddingApi.embedding(internalRequest, new HttpHeaders())
                    );

                    if (apiEmbeddingResponse == null) {
                        log.warn("No apiEmbeddingResponse returned for request: {}", request);
                        return new EmbeddingResponse(List.of());
                    }

                    EmbeddingApi.EmbeddingResponse response = apiEmbeddingResponse.getBody();

                    if (response == null) {
                        log.warn("No response for request: {}", request);
                        return new EmbeddingResponse(List.of());
                    }

                    var metadata = new EmbeddingResponseMetadata(internalRequest.modelUri(), new DefaultUsage(0, 0, 0, null));

                    List<Embedding> embeddings = List.of(new Embedding(response.embedding(), 0));

                    EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, metadata);

                    observationContext.setResponse(embeddingResponse);
                    return embeddingResponse;
                });
    }

    protected EmbeddingRequestSpec buildRequest(EmbeddingRequest request, ru.ksoft.springaiyandexgpt.constants.EmbeddingModel model) {
        YandexGptEmbeddingOptions requestOptions = null;
        if (request.getOptions() != null) {
            requestOptions = ModelOptionsUtils.copyToTarget(request.getOptions(), EmbeddingOptions.class, YandexGptEmbeddingOptions.class);
        }
        YandexGptEmbeddingOptions options = defaultOptions.merge(requestOptions);
        return EmbeddingRequestSpec.builder()
                .model(model)
                .text(String.join(";\n", request.getInstructions()))
                .folderId(options.getFolderId())
                .dim(String.valueOf(options.getDimensions()))
                .build();
    }

    @Override
    public @NonNull String getEmbeddingContent(Document document) {
        return document.getFormattedContent(metadataMode);
    }

    @Override
    public @NonNull List<float[]> embed(List<String> query) {
        EmbeddingResponse response = call(
                new EmbeddingRequest(query, EmbeddingOptions.builder().build()),
                EmbeddingModel.QUERY
        );
        return response.getResults().stream().map(Embedding::getOutput).toList();
    }


    @Override
    public float[] embed(Document document) {
        EmbeddingResponse response = call(
                new EmbeddingRequest(List.of(document.getFormattedContent(metadataMode)), EmbeddingOptions.builder().build()),
                EmbeddingModel.DOC
        );
        return response.getResult().getOutput();
    }

    @Override
    public int dimensions() {
        if (this.embeddingDimensions.get() < 0 && this.defaultOptions.getModel() != null) {
            this.embeddingDimensions.set(dimensions(this, this.defaultOptions.getModel(), DUMMY_TEXT));
        }

        return super.dimensions();
    }
}
