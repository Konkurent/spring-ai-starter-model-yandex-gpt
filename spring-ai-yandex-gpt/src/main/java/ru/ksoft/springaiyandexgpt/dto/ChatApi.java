package ru.ksoft.springaiyandexgpt.dto;

import com.fasterxml.jackson.annotation.*;
import org.springframework.ai.chat.metadata.Usage;
import ru.ksoft.springaiyandexgpt.constants.*;
import ru.ksoft.springaiyandexgpt.text.YandexGptChatOptions;

import java.util.List;

public class ChatApi {

    @JsonPropertyOrder(
            {
                    "modelUri",
                    "completionOptions",
                    "messages",
                    "jsonObject",
                    "jsonSchema",
                    "parallelToolCalls",
                    "toolChoice"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ChatCompletionRequest(
            @JsonIgnore Model model,
            @JsonIgnore String folderId,
            @JsonProperty("completionOptions") YandexGptCompletionOptions completionOptions,
            @JsonProperty("messages") List<Message> messages,
            @JsonProperty("jsonObject") Boolean jsonObject,
            @JsonProperty("jsonSchema") JsonSchema jsonSchema,
            @JsonProperty("parallelToolCalls") Boolean parallelToolCalls,
            @JsonProperty("toolChoice") ToolChoice toolChoice
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public Builder mutate() {
            return new Builder(this);
        }

        @JsonGetter("modelUri")
        public String modelUri() {
            return model.getUri(folderId());
        }


        public static class Builder {
            private Model model;
            private String folderId;
            private YandexGptCompletionOptions completionOptions;
            private List<Message> messages;
            private Boolean jsonObject;
            private JsonSchema jsonSchema;
            private Boolean parallelToolCalls;
            private ToolChoice toolChoice;

            public Builder() {}

            public Builder(ChatCompletionRequest from) {
                this.model = from.model;
                this.folderId = from.folderId;
                this.completionOptions = from.completionOptions;
                this.messages = from.messages;
                this.jsonObject = from.jsonObject;
                this.jsonSchema = from.jsonSchema;
                this.parallelToolCalls = from.parallelToolCalls;
                this.toolChoice = from.toolChoice;
            }

            @JsonIgnore
            public Builder model(Model model) {
                this.model = model;
                return this;
            }

            @JsonIgnore
            public Builder folderId(String folderId) {
                this.folderId = folderId;
                return this;
            }

            @JsonProperty("completionOptions")
            public Builder completionOptions(YandexGptCompletionOptions completionOptions) {
                this.completionOptions = completionOptions;
                return this;
            }

            @JsonProperty("messages")
            public Builder messages(List<Message> messages) {
                this.messages = messages;
                return this;
            }

            @JsonProperty("jsonObject")
            public Builder jsonObject(Boolean jsonObject) {
                this.jsonObject = jsonObject;
                return this;
            }

            @JsonProperty("jsonSchema")
            public Builder jsonSchema(JsonSchema jsonSchema) {
                this.jsonSchema = jsonSchema;
                return this;
            }

            @JsonProperty("parallelToolCalls")
            public Builder parallelToolCalls(Boolean parallelToolCalls) {
                this.parallelToolCalls = parallelToolCalls;
                return this;
            }

            @JsonProperty("toolChoice")
            public Builder toolChoice(ToolChoice toolChoice) {
                this.toolChoice = toolChoice;
                return this;
            }

            public ChatCompletionRequest build() {
                return new ChatCompletionRequest(this.model, this.folderId, this.completionOptions, this.messages, this.jsonObject, this.jsonSchema, this.parallelToolCalls, this.toolChoice);
            }
        }
    }

    @JsonPropertyOrder(
            {
                    "stream",
                    "temperature",
                    "maxTokens",
                    "reasoningOptions"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record YandexGptCompletionOptions(
            @JsonProperty("stream") Boolean stream,
            @JsonProperty("temperature") Float temperature,
            @JsonProperty("maxTokens") Integer maxTokens,
            @JsonProperty("reasoningOptions") ReasoningOptions reasoningOptions
    ) {

        public YandexGptCompletionOptions(Boolean stream, YandexGptChatOptions options) {
            this(
                    Boolean.TRUE.equals(stream),
                    options.getTemperature(),
                    options.getMaxTokens(),
                    options.getReasoningMode() != null ? new ReasoningOptions(options.getReasoningMode()) : null
            );

        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder mutate() {
            return new Builder(this);
        }

        public static class Builder {
            private Boolean stream;
            private Float temperature;
            private Integer maxTokens;
            private ReasoningOptions reasoningOptions;

            Builder() {
            }

            Builder(YandexGptCompletionOptions from) {
                this.stream = from.stream;
                this.temperature = from.temperature;
                this.maxTokens = from.maxTokens;
                this.reasoningOptions = from.reasoningOptions;
            }

            @JsonProperty("stream")
            public Builder stream(Boolean stream) {
                this.stream = stream;
                return this;
            }

            @JsonProperty("temperature")
            public Builder temperature(Float temperature) {
                this.temperature = temperature;
                return this;
            }

            @JsonProperty("maxTokens")
            public Builder maxTokens(Integer maxTokens) {
                this.maxTokens = maxTokens;
                return this;
            }

            public Builder withReasoningMode(ReasoningMode mode) {
                this.reasoningOptions = new ReasoningOptions(mode);
                return this;
            }

            @JsonProperty("reasoningOptions")
            public Builder reasoningOptions(ReasoningOptions reasoningOptions) {
                this.reasoningOptions = reasoningOptions;
                return this;
            }

            public YandexGptCompletionOptions build() {
                return new YandexGptCompletionOptions(this.stream, this.temperature, this.maxTokens, this.reasoningOptions);
            }

            public String toString() {
                return "YandexGptApi.YandexGptCompletionOptions.Builder(stream=" + this.stream + ", temperature=" + this.temperature + ", maxTokens=" + this.maxTokens + ", reasoningOptions=" + this.reasoningOptions + ")";
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReasoningOptions(
            ReasoningMode mode
    ) {}

    public interface Message {
        Role role();

    }

    @JsonPropertyOrder(
            {
                    "role",
                    "text"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TextualMessage(
            @JsonIgnore Role role,
            String text
    ) implements Message {

        public static Builder builder() {
            return new Builder();
        }

        public TextualMessage.Builder mutate() {
            return new Builder(this);
        }

        public static class Builder {
            private Role role;
            private String text;

            Builder() {}

            Builder(TextualMessage message) {
                this.role = message.role;
                this.text = message.text;
            }

            @JsonIgnore
            public Builder role(Role role) {
                this.role = role;
                return this;
            }

            public Builder text(String text) {
                this.text = text;
                return this;
            }

            public TextualMessage build() {
                return new TextualMessage(this.role, this.text);
            }

            public String toString() {
                return "YandexGptApi.TextualMessage.Builder(role=" + this.role + ", text=" + this.text + ")";
            }
        }
    }

    public record JsonSchema(
            Object schema
    ) {}

    // Future
    public record ToolChoice (
            ToolChoiceMode mode,
            String functionName
    ) {}

    public record ResultHolder<T>(
            T result
    ) {}

    @JsonPropertyOrder(
            {
                    "alternatives",
                    "usage",
                    "modelVersion"
            }
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletionResponse(
            @JsonProperty("alternatives") List<Alternative> alternatives,
            @JsonProperty("usage") ContentUsage usage,
            @JsonProperty("modelVersion") String modelVersion
    ) {

        public ChatCompletionResponse.Builder mutate() {
            return new Builder(this);
        }

        public static ChatCompletionResponse.Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private List<Alternative> alternatives;
            private ContentUsage usage;
            private String modelVersion;

            Builder() {}

            Builder(ChatCompletionResponse response) {
                this.alternatives = response.alternatives;
                this.usage = response.usage;
                this.modelVersion = response.modelVersion;
            }

            @JsonProperty("alternatives")
            public Builder alternatives(List<Alternative> alternatives) {
                this.alternatives = alternatives;
                return this;
            }

            @JsonProperty("usage")
            public Builder usage(ContentUsage usage) {
                this.usage = usage;
                return this;
            }

            @JsonProperty("modelVersion")
            public Builder modelVersion(String modelVersion) {
                this.modelVersion = modelVersion;
                return this;
            }

            public ChatCompletionResponse build() {
                return new ChatCompletionResponse(this.alternatives, this.usage, this.modelVersion);
            }

            public String toString() {
                return "YandexGptApi.ChatCompletionResponse.Builder(alternatives=" + this.alternatives + ", usage=" + this.usage + ", modelVersion=" + this.modelVersion + ")";
            }
        }
    }


    @JsonPropertyOrder(
            {
                    "message",
                    "status"
            }
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alternative(
            @JsonProperty("message") TextualMessage message,
            @JsonProperty("status") CompletionResponseStatus status
    ) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentUsage(
            @JsonProperty("inputTextTokens") String inputTextTokens,
            @JsonProperty("completionTokens") String completionTokens,
            @JsonProperty("totalTokens") String totalTokens,
            @JsonProperty("completionTokensDetails") CompletionTokensDetails completionTokensDetails
    ) implements Usage {
        @Override
        public Integer getPromptTokens() {
            return inputTextTokens != null ? Integer.valueOf(inputTextTokens) : null;
        }

        @Override
        public Integer getCompletionTokens() {
            return completionTokens != null ? Integer.valueOf(completionTokens) : null;
        }

        @Override
        public Integer getTotalTokens() {
            return totalTokens != null ? Integer.valueOf(totalTokens) : null;
        }

        @Override
        public Object getNativeUsage() {
            return this;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompletionTokensDetails(
            @JsonProperty("reasoningTokens") String reasoningTokens
    ) {}

}
