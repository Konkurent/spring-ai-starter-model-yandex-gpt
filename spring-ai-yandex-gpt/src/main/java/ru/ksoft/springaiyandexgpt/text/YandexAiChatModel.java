package ru.ksoft.springaiyandexgpt.text;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.metadata.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import ru.ksoft.springaiyandexgpt.constants.CompletionResponseStatus;
import ru.ksoft.springaiyandexgpt.constants.Role;
import ru.ksoft.springaiyandexgpt.dto.ChatApi;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.*;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.utils.RetryExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring AI {@link ChatModel} backed by Yandex Foundation Models chat completions.
 * <p>
 * Converts {@link Prompt} messages to the Yandex REST format, calls {@link YandexAiChatApi}, and maps
 * the response into {@link ChatResponse}. Supports synchronous calls and streaming; tool calling is
 * not supported. Uses Micrometer observations when an {@link ObservationRegistry} is configured.
 */
@Slf4j
public class YandexAiChatModel implements ChatModel {

    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

    public static final String PROVIDER_NAME = "yandex_gpt";

    /**
     * The default options used for the chat completion requests.
     */
    private final YandexAiChatOptions defaultOptions;

    /** HTTP client for the completions endpoint. */
    private final YandexAiChatApi chatApi;

    private final RetryTemplate retryTemplate;

    private final Retry retry;

    /**
     * Observation registry used for instrumentation.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Conventions to use for generating observations.
     */
    @Setter
    private ChatModelObservationConvention observationConvention;

    public YandexAiChatModel(
            YandexAiChatApi chatApi,
            YandexAiChatOptions defaultOptions,
            ClientSpec.RetrySpec retrySpec,
            ObservationRegistry observationRegistry
    ) {
        this(chatApi, defaultOptions, retrySpec, observationRegistry, DEFAULT_OBSERVATION_CONVENTION);
    }

    public YandexAiChatModel(
            YandexAiChatApi chatApi,
            YandexAiChatOptions defaultOptions,
            ClientSpec.RetrySpec retrySpec,
            ObservationRegistry observationRegistry,
            ChatModelObservationConvention observationConvention
    ) {
        Assert.notNull(chatApi, "YandexChatApi cannot be null");
        Assert.notNull(defaultOptions, "defaultOptions cannot be null");
        Assert.notNull(retrySpec, "retrySpec cannot be null");
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        Assert.notNull(observationConvention, "observationConvention cannot be null");
        this.chatApi = chatApi;
        this.defaultOptions = defaultOptions;
        this.retryTemplate = retrySpec.retryTemplate();
        this.retry = retrySpec.retry();
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }

    @NotNull
    @Override
    public ChatResponse call(Prompt prompt) {
        YandexGptPrompt yaPrompt = new YandexGptPrompt(prompt);
        ChatCompletionRequest request = createRequest(yaPrompt, false);
        HttpHeaders httpHeaders = new HttpHeaders();
        yaPrompt.getOptions().getHttpHeaders().forEach(httpHeaders::add);
        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider(PROVIDER_NAME)
                .build();

        return Objects.requireNonNull(ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(
                        this.observationConvention,
                        DEFAULT_OBSERVATION_CONVENTION,
                        () -> observationContext,
                        this.observationRegistry
                )
                .observe(() -> {

                    ResponseEntity<ResultHolder<ChatCompletionResponse>> completionEntity = RetryExecutor.execute(this.retryTemplate,
                            (ctx) -> yaPrompt.getOptions().getHttpHeaders().isEmpty()
                                    ? this.chatApi.completionEntity(request)
                                    : this.chatApi.completionEntity(request, httpHeaders)
                    );

                    var resultHolder = completionEntity.getBody();
                    if (resultHolder == null) {
                        log.warn("No chat result returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    ChatCompletionResponse chatCompletion = resultHolder.result();
                    if (chatCompletion == null) {
                        log.warn("No chat completion returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    List<Alternative> alternatives = chatCompletion.alternatives();
                    if (alternatives == null) {
                        log.warn("No alternatives returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    List<Generation> generations = alternatives.stream()
                            .map(alternative -> buildGeneration(
                                    alternative,
                                    Map.of(
                                            "role", alternative.message().role() != null ? alternative.message().role().name() : "",
                                            "finishReason", alternative.status()
                                    )
                            )).toList();

                    ChatResponseMetadata.Builder metadataBuilder = ChatResponseMetadata.builder()
                            .usage(chatCompletion.usage() != null ? chatCompletion.usage() : new EmptyUsage())
                            .keyValue("modelVersion", chatCompletion.modelVersion());
                    completionEntity.getHeaders().forEach(metadataBuilder::keyValue);

                    ChatResponse chatResponse = new ChatResponse(generations, metadataBuilder.build());

                    observationContext.setResponse(chatResponse);

                    return chatResponse;
                }));
    }


    private Generation buildGeneration(Alternative alternative) {
        return buildGeneration(alternative, new HashMap<>());
    }

    private Generation buildGeneration(Alternative alternative, Map<String, Object> metadata) {

        var generationMetadataBuilder = ChatGenerationMetadata.builder()
                .metadata(metadata)
                .finishReason(alternative.status() != null ? alternative.status().name() : "");

        List<Media> media = new ArrayList<>();
        String textContent = alternative.message().text();

        var assistantMessage = AssistantMessage.builder()
                .content(textContent)
                .properties(metadata)
                .media(media)
                .build();
        return new Generation(assistantMessage, generationMetadataBuilder.build());
    }

    private static final Set<MessageType> SUPPORTED_MESSAGE_TYPES = Set.of(MessageType.USER, MessageType.SYSTEM, MessageType.ASSISTANT);

    private ChatCompletionRequest createRequest(YandexGptPrompt prompt, boolean stream) {
        List<ChatApi.Message> chatCompletionMessages = prompt.getInstructions().stream()
                .filter(message -> SUPPORTED_MESSAGE_TYPES.contains(message.getMessageType()))
                .map(message -> TextualMessage.builder().role(Role.valueOf(message.getMessageType().name())).build())
                .collect(Collectors.toList());

        YandexAiChatOptions requestOptions = prompt.getOptions();

        return ChatCompletionRequest.builder()
                .model(requestOptions.getCompletionModel())
                .folderId(requestOptions.getFolderId())
                .completionOptions(
                        YandexGptCompletionOptions.builder()
                                .stream(stream)
                                .temperature(requestOptions.getTemperature() != null ? requestOptions.getTemperature().floatValue() : null)
                                .maxTokens(requestOptions.getMaxTokens())
                                .reasoningOptions(new ReasoningOptions(requestOptions.getReasoningMode()))
                                .build()
                )
                .messages(chatCompletionMessages)
                .build();
    }

    @Override
    public YandexAiChatOptions getDefaultOptions() {
        return defaultOptions.copy();
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.deferContextual(ctx -> {
            YandexGptPrompt yaPrompt = new YandexGptPrompt(prompt);

            ChatCompletionRequest request = createRequest(yaPrompt, true);

            HttpHeaders httpHeaders = new HttpHeaders();
            yaPrompt.getOptions().getHttpHeaders().forEach(httpHeaders::add);


            Flux<ChatCompletionResponse> stream = chatApi.completionStream(request, httpHeaders)
                    .retryWhen(retry)
                    .takeUntil(
                            response -> response.alternatives().stream()
                                    .anyMatch(alternative -> alternative.status() == CompletionResponseStatus.ALTERNATIVE_STATUS_FINAL)
                    );

            final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                    .prompt(prompt)
                    .provider(PROVIDER_NAME)
                    .build();

            Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
                    this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                    this.observationRegistry);

            observation.parentObservation(ctx.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

            Flux<ChatResponse> flux = stream.index().map(tuple -> {
                try {
                    String id = String.valueOf(tuple.getT1());
                    List<Generation> generations = tuple.getT2().alternatives().stream().map(alternative ->
                        buildGeneration(
                                alternative,
                                Map.of(
                                        "id", id,
                                        "role", alternative.message().role() != null ? alternative.message().role().name() : "",
                                        "finishReason", alternative.status()
                                )
                        )
                    ).toList();
                    ChatCompletionResponse resp = tuple.getT2();
                    ChatResponseMetadata.Builder metadataBuilder = ChatResponseMetadata.builder()
                            .usage(resp.usage() != null ? resp.usage() : new EmptyUsage())
                            .keyValue("modelVersion", resp.modelVersion());
                    return new ChatResponse(generations, metadataBuilder.build());
                }
                catch (Exception e) {
                    log.error("Error processing chat completion", e);
                    return new ChatResponse(List.of());
                }
            })
                    .doOnError(observation::error)
                    .doFinally(s -> observation.stop())
                    .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));

            return new MessageAggregator().aggregate(flux, observationContext::setResponse);
        });
    }


    private class YandexGptPrompt extends Prompt {
        @Delegate private final Prompt prompt;

        public YandexGptPrompt(Prompt prompt) {
            YandexAiChatOptions requestOptions = null;
            if (prompt.getOptions() != null && prompt.getOptions() instanceof ToolCallingChatOptions) {
                throw new UnsupportedOperationException("Tool calling is not supported");
            }
            if (prompt.getOptions() != null && prompt.getOptions() instanceof YandexAiChatOptions) {
                requestOptions = (YandexAiChatOptions) prompt.getOptions();
            } else if (prompt.getOptions() != null) {
                requestOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class, YandexAiChatOptions.class);
            }
            this.prompt = new Prompt(prompt.getInstructions(), defaultOptions.merge(requestOptions));
        }

        @NonNull
        public YandexAiChatOptions getOptions() {
            return (YandexAiChatOptions) prompt.getOptions();
        }
    }

}
