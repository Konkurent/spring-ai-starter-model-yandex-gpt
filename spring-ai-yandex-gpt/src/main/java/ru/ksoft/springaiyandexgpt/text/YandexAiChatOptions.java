package ru.ksoft.springaiyandexgpt.text;

import com.github.victools.jsonschema.generator.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.StructuredOutputChatOptions;
import org.springframework.util.Assert;
import ru.ksoft.springaiyandexgpt.constants.CompletionModel;
import ru.ksoft.springaiyandexgpt.constants.ReasoningMode;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-request options for {@link YandexAiChatModel}: model URI, folder, sampling, reasoning mode,
 * optional JSON schema for structured output, and extra HTTP headers.
 * <p>
 * Values merge with defaults: see {@link #merge(YandexAiChatOptions)}. Several Spring AI
 * {@link org.springframework.ai.chat.prompt.ChatOptions} knobs are intentionally unsupported and
 * return {@code null} or log a warning.
 */
@Getter
@Setter
@EqualsAndHashCode
@Slf4j
@AllArgsConstructor
public class YandexAiChatOptions implements StructuredOutputChatOptions {

    private CompletionModel completionModel;

    private String folderId;

    @Getter(AccessLevel.NONE)
    private Float temperature = 0.7F;

    private Class<?> outputClass;

    private String outputSchema;

    private Integer maxTokens;

    private ReasoningMode reasoningMode;

    private Map<String, String> httpHeaders = new HashMap<>();

    private List<String> stopSequences = new ArrayList<>();

    public YandexAiChatOptions.Builder mutate() {
        return new Builder(this);
    }

    public YandexAiChatOptions merge(YandexAiChatOptions other) {
        Assert.notNull(other, "YandexGptChatOptions must not be null");
        YandexAiChatOptions.Builder mutator = mutate();
        if (other.getCompletionModel() != null) {
            mutator.model(other.getCompletionModel());
        }
        if (other.getTemperature() != null && Double.compare(other.getTemperature(), 0.7d) != 0) {
            mutator.temperature(other.getTemperature().floatValue());
        }
        if (other.getOutputClass() != null) {
            mutator.outputClass(other.getOutputClass());
        }
        if (other.getOutputSchema() != null) {
            mutator.outputSchema(other.getOutputSchema());
        }
        if (other.getMaxTokens() != null) {
            mutator.maxTokens(other.getMaxTokens());
        }
        List<String> internalStopSequences = new ArrayList<>();
        if (stopSequences != null && !stopSequences.isEmpty()) {
            internalStopSequences.addAll(stopSequences);
        }
        if (other.getStopSequences() != null && !other.getStopSequences().isEmpty()) {
            internalStopSequences.addAll(other.getStopSequences());
        }
        mutator.stopSequences(internalStopSequences);
        Map<String, String> internalHttpHeaders = new HashMap<>();
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            internalHttpHeaders.putAll(httpHeaders);
        }
        if (other.getHttpHeaders() != null && !other.getHttpHeaders().isEmpty()) {
            internalHttpHeaders.putAll(other.getHttpHeaders());
        }
        mutator.httpHeaders(internalHttpHeaders);
        if (other.getReasoningMode() != null) {
            mutator.reasoningMode(other.getReasoningMode());
        }
        if (other.getFolderId() != null) {
            mutator.folderId(other.getFolderId());
        }
        return mutator.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getModel() {
        return completionModel != null ? completionModel.getName() : null;
    }

    @Override
    public Double getTemperature() {
        return temperature != null ? temperature.doubleValue() : null;
    }

    public void setOutputClass(Class<?> outputClass) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(outputClass);
        outputSchema = jsonSchema.toPrettyString();
    }

    @Override
    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    @Nullable
    @Override
    public Double getFrequencyPenalty() {
        log.warn("Frequencies are not supported.");
        return null;
    }

    @Nullable
    @Override
    public Double getPresencePenalty() {
        log.warn("Presence penalty are not supported.");
        return null;
    }


    @Nullable
    @Override
    public Integer getTopK() {
        log.warn("Top K are not supported.");
        return null;
    }

    @Nullable
    @Override
    public Double getTopP() {
        log.warn("Top P are not supported.");
        return null;
    }

    @Override
    public <T extends ChatOptions> T copy() {
        return (T) mutate().build();
    }

    public static class Builder {
        private CompletionModel completionModel;
        private String folderId;
        private Float temperature;
        private Class<?> outputClass;
        private String outputSchema;
        private Integer maxTokens;
        private ReasoningMode reasoningMode;
        private List<String> stopSequences;
        private Map<String, String> httpHeaders;

        Builder() {}

        Builder(YandexAiChatOptions completionOptions) {
            this.completionModel = completionOptions.getCompletionModel();
            this.folderId = completionOptions.getFolderId();
            this.temperature = completionOptions.getTemperature() != null ? completionOptions.getTemperature().floatValue() : null;
            this.outputClass = completionOptions.getOutputClass();
            this.outputSchema = completionOptions.getOutputSchema();
            this.reasoningMode = completionOptions.getReasoningMode();
            this.maxTokens = completionOptions.getMaxTokens();
            this.stopSequences = completionOptions.getStopSequences();
        }

        public Builder model(CompletionModel model) {
            this.completionModel = model;
            return this;
        }

        public  Builder folderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder temperature(Float temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder outputClass(Class<?> outputClass) {
            SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
            SchemaGeneratorConfig config = configBuilder.build();
            SchemaGenerator generator = new SchemaGenerator(config);
            JsonNode jsonSchema = generator.generateSchema(outputClass);
            outputSchema = jsonSchema.toPrettyString();
            return this;
        }

        public Builder outputSchema(String outputSchema) {
            this.outputSchema = outputSchema;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
            return this;
        }

        public Builder httpHeaders(Map<String, String> httpHeaders) {
            this.httpHeaders = httpHeaders;
            return this;
        }

        public Builder reasoningMode(ReasoningMode reasoningMode) {
            this.reasoningMode = reasoningMode;
            return this;
        }

        public YandexAiChatOptions build() {
            return new YandexAiChatOptions(this.completionModel, this.folderId, this.temperature, this.outputClass, this.outputSchema, this.maxTokens, this.reasoningMode, this.httpHeaders, this.stopSequences);
        }

        public String toString() {
            return "YandexGptChatOptions.Builder(completionModel=" + this.completionModel + ", temperature=" + this.temperature + ", outputClass=" + this.outputClass + ", outputSchema=" + this.outputSchema + ", maxTokens=" + this.maxTokens + ", stopSequences=" + this.stopSequences + ")";
        }

    }
}
