package ru.ksoft.springaiyandexgpt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ru.ksoft.springaiyandexgpt.image.YandexAiImageOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Yandex AI image generation DTOs: request with prompts and options, operation response, and compact image payload.
 */
public class ImageApi {

    /** Image API request body: model URI, weighted prompt messages, and generation options. */
    @JsonPropertyOrder(
            {
                    "modelUri",
                    "messages",
                    "generationOptions"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record YandexAiImageRequest(
            @JsonProperty("modelUri") String modelUri,
            @JsonProperty("messages") List<Message> messages,
            @JsonProperty("generationOptions") GenerationOptions generationOptions
    ) {
        public static Builder builder() {
            return new Builder();
        }

        /** Prompt part: text and influence weight. */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Message(
                @JsonProperty("text") String text,
                @JsonProperty("weight") String weight
        ) {
            public Message(String text, Float weight) {
                this(
                        text,
                        weight != null ? weight.toString() : null
                );
            }
        }

        /** MIME type, seed, and aspect ratio; can be built from {@link YandexAiImageOptions}. */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record GenerationOptions(
                @JsonProperty("mimeType") String mimeType,
                @JsonProperty("seed") String seed,
                @JsonProperty("aspectRatio") AspectRatio aspectRatio
        ) {

            public GenerationOptions(YandexAiImageOptions options) {
                this(
                        options.getMimeType().getType(),
                        options.getSeed() != null ? options.getSeed().toString() : null,
                        new AspectRatio(options)
                );
            }

            /** Width/height ratio as string coefficients for the API. */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record AspectRatio(
                    @JsonProperty("widthRatio") String widthRatio,
                    @JsonProperty("heightRatio") String heightRatio
            ) {

                public AspectRatio(YandexAiImageOptions options) {
                    this(
                            options.getWidthRatio() != null ? options.getWidthRatio().toString() : null,
                            options.getHeightRatio() != null ? options.getHeightRatio().toString() : null
                    );
                    if (options.getHeightRatio() == null && options.getWidthRatio() == null) {
                        throw new IllegalArgumentException("Height and Width ratio must not be null");
                    }
                }

            }

        }

        public static class Builder {
            private String modelUri;
            private List<Message> messages;
            private GenerationOptions generationOptions;

            Builder() {}

            @JsonProperty("modelUri")
            public Builder modelUri(String modelUri) {
                this.modelUri = modelUri;
                return this;
            }

            @JsonProperty("messages")
            public Builder messages(List<Message> messages) {
                this.messages = messages;
                return this;
            }

            @JsonProperty("generationOptions")
            public Builder generationOptions(GenerationOptions generationOptions) {
                this.generationOptions = generationOptions;
                return this;
            }

            public YandexAiImageRequest build() {
                return new YandexAiImageRequest(this.modelUri, this.messages, this.generationOptions);
            }

            public String toString() {
                return "ImageApi.YandexAiImageRequest.Builder(modelUri=" + this.modelUri + ", messages=" + this.messages + ", generationOptions=" + this.generationOptions + ")";
            }
        }
    }

    /**
     * Long-running operation response: metadata, error, and {@link ImageDetails} when done.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record YandexAiImageResponse(
            @JsonProperty("id") String id,
            @JsonProperty("description") String description,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("modifiedAt") String modifiedAt,
            @JsonProperty("done") Boolean done,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("error") ErrorDetails error,
            @JsonProperty("response") ImageDetails imageDetails
    ) {

        /** Operation error (code, message, details). */
        public record ErrorDetails(
                @JsonProperty("code") Integer code,
                @JsonProperty("message") String message,
                @JsonProperty("details") List<Map<String, Object>> details
        ) {}

        /** Generated image data (base64) and model version. */
        public record ImageDetails(
                @JsonProperty("image") String image,
                @JsonProperty("modelVersion") String modelVersion
        ) {}
    }

    /** Minimal successful response body: image payload and model version only. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ImageResponseSpec(
            @JsonProperty("image") String image,
            @JsonProperty("modelVersion") String modelVersion
    ) {}


}
