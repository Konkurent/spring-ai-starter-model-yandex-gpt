package ru.ksoft.springaiyandexgpt.text;

import com.github.victools.jsonschema.generator.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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


@Data
@Slf4j
@AllArgsConstructor
public class YandexGptChatOptions implements StructuredOutputChatOptions {

    private CompletionModel model;

    private String folderId;

    private Float temperature = 0.7F;

    private Class<?> outputClass;

    private String outputSchema;

    private Integer maxTokens;

    private ReasoningMode reasoningMode;

    private Map<String, String> httpHeaders = new HashMap<>();

    private List<String> stopSequences = new ArrayList<>();

    public YandexGptChatOptions.Builder mutate() {
        return new Builder(this);
    }

    public YandexGptChatOptions merge(YandexGptChatOptions other) {
        Assert.notNull(other, "YandexGptChatOptions must not be null");
        YandexGptChatOptions.Builder mutator = mutate();
        if (other.getModel() != null) {
            mutator.model(other.getModel());
        }
        if ((other.getTemperature() != null && other.getTemperature() != 0.7D)) {
            mutator.temperature(other.getTemperature());
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
        private CompletionModel model;
        private String folderId;
        private Float temperature;
        private Class<?> outputClass;
        private String outputSchema;
        private Integer maxTokens;
        private ReasoningMode reasoningMode;
        private List<String> stopSequences;
        private Map<String, String> httpHeaders;

        Builder() {}

        Builder(YandexGptChatOptions completionOptions) {
            this.model = completionOptions.getModel();
            this.folderId = completionOptions.getFolderId();
            this.temperature = completionOptions.getTemperature();
            this.outputClass = completionOptions.getOutputClass();
            this.outputSchema = completionOptions.getOutputSchema();
            this.reasoningMode = completionOptions.getReasoningMode();
            this.maxTokens = completionOptions.getMaxTokens();
            this.stopSequences = completionOptions.getStopSequences();
        }

        public Builder model(CompletionModel model) {
            this.model = model;
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
            this.outputClass = outputClass;
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

        public YandexGptChatOptions build() {
            return new YandexGptChatOptions(this.model, this.folderId, this.temperature, this.outputClass, this.outputSchema, this.maxTokens, this.reasoningMode, this.httpHeaders, this.stopSequences);
        }

        public String toString() {
            return "YandexGptChatOptions.Builder(model=" + this.model + ", temperature=" + this.temperature + ", outputClass=" + this.outputClass + ", outputSchema=" + this.outputSchema + ", maxTokens=" + this.maxTokens + ", stopSequences=" + this.stopSequences + ")";
        }

    }
}
