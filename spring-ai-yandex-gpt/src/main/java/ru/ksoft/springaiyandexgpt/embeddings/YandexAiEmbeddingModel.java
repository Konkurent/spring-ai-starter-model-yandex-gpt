package ru.ksoft.springaiyandexgpt.embeddings;

import io.micrometer.observation.ObservationRegistry;
import lombok.Setter;
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
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.dto.EmbeddingApi.EmbeddingRequestSpec;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatModel;
import ru.ksoft.springaiyandexgpt.utils.RetryExecutor;

import java.util.List;

/**
 * Spring AI {@link org.springframework.ai.embedding.EmbeddingModel} for Yandex text embeddings.
 * <p>
 * Builds requests from {@link EmbeddingRequest}, applies folder and dimension defaults from
 * {@link YandexAiEmbeddingOptions}, and delegates HTTP to {@link YandexAiEmbeddingApi}. Records
 * Micrometer observations when configured.
 */
@Slf4j
public class YandexAiEmbeddingModel extends AbstractEmbeddingModel {

    private static final String DUMMY_TEXT = "Dummy message to resolve embedding vector dimension";

    private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

    private final YandexAiEmbeddingOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    private final ObservationRegistry observationRegistry;

    @Setter
    private EmbeddingModelObservationConvention observationConvention;

    private final YandexAiEmbeddingApi embeddingApi;

    private final MetadataMode metadataMode;

    public YandexAiEmbeddingModel(YandexAiEmbeddingApi embeddingApi, MetadataMode metadataMode, YandexAiEmbeddingOptions options,
                                  ClientSpec.RetrySpec retrySpec, ObservationRegistry observationRegistry) {
        this(embeddingApi, metadataMode, options, retrySpec, observationRegistry, DEFAULT_OBSERVATION_CONVENTION);
    }

    public YandexAiEmbeddingModel(YandexAiEmbeddingApi embeddingApi, MetadataMode metadataMode, YandexAiEmbeddingOptions options,
                                  ClientSpec.RetrySpec retrySpec, ObservationRegistry observationRegistry, EmbeddingModelObservationConvention observationConvention) {
        Assert.notNull(embeddingApi, "embeddingApi must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retrySpec, "retrySpec must not be null");
        Assert.notNull(observationRegistry, "observationRegistry must not be null");

        this.embeddingApi = embeddingApi;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
        this.retryTemplate = retrySpec.retryTemplate();
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }


    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return call(request, EmbeddingModel.DOC);
    }

    /**
     * Embeds the request using the given {@link ru.ksoft.springaiyandexgpt.constants.EmbeddingModel}
     * template (document vs query vs tuning).
     *
     * @param request Spring AI embedding request
     * @param model   which Yandex embedding URI template to apply
     * @return embedding vectors and metadata
     */
    public EmbeddingResponse call(EmbeddingRequest request, EmbeddingModel model) {
        var observationContext = EmbeddingModelObservationContext.builder()
                .embeddingRequest(request)
                .provider(YandexAiChatModel.PROVIDER_NAME)
                .build();

        EmbeddingRequestSpec requestSpec = buildRequest(request, model);
        YandexAiEmbeddingApi.EmbeddingRequest internalRequest = new YandexAiEmbeddingApi.EmbeddingRequest(
                requestSpec.modelUri(),
                requestSpec.text(),
                requestSpec.dim()
        );

        return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    ResponseEntity<YandexAiEmbeddingApi.EmbeddingResponse> apiEmbeddingResponse = RetryExecutor.execute(this.retryTemplate,
                            (ctx) -> embeddingApi.embedding(internalRequest, new HttpHeaders())
                    );

                    if (apiEmbeddingResponse == null) {
                        log.warn("No apiEmbeddingResponse returned for request: {}", request);
                        return new EmbeddingResponse(List.of());
                    }

                    YandexAiEmbeddingApi.EmbeddingResponse response = apiEmbeddingResponse.getBody();

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
        YandexAiEmbeddingOptions requestOptions = null;
        if (request.getOptions() != null) {
            requestOptions = ModelOptionsUtils.copyToTarget(request.getOptions(), EmbeddingOptions.class, YandexAiEmbeddingOptions.class);
        }
        YandexAiEmbeddingOptions options = defaultOptions.merge(requestOptions);
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
