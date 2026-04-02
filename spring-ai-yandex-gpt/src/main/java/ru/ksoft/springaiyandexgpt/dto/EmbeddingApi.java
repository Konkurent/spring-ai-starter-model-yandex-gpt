package ru.ksoft.springaiyandexgpt.dto;

import com.fasterxml.jackson.annotation.*;
import ru.ksoft.springaiyandexgpt.constants.EmbeddingModel;

public class EmbeddingApi {

    @JsonPropertyOrder(
            {
                    "modelUri",
                    "text",
                    "dim"
            }
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EmbeddingRequestSpec(
            @JsonIgnore EmbeddingModel model,
            @JsonIgnore String folderId,
            @JsonProperty("text") String text,
            @JsonProperty("dim") String dim
    ) {

        public EmbeddingRequestSpec.Builder mutate(EmbeddingRequestSpec other) {
            return new Builder(other);
        }

        public static Builder builder() {
            return new Builder();
        }

        @JsonGetter("modelUri")
        public String modelUri() {
            return model.getUri(folderId);
        }

        public static class Builder {
            private EmbeddingModel model;
            private String folderId;
            private String text;
            private String dim;

            Builder() {}

            Builder(EmbeddingRequestSpec other) {
                this.model = other.model;
                this.folderId = other.folderId;
                this.text = other.text;
                this.dim = other.dim;
            }

            @JsonIgnore
            public Builder model(EmbeddingModel model) {
                this.model = model;
                return this;
            }

            @JsonIgnore
            public Builder folderId(String folderId) {
                this.folderId = folderId;
                return this;
            }

            @JsonProperty("text")
            public Builder text(String text) {
                this.text = text;
                return this;
            }

            @JsonProperty("dim")
            public Builder dim(String dim) {
                this.dim = dim;
                return this;
            }

            public EmbeddingRequestSpec build() {
                return new EmbeddingRequestSpec(this.model, this.folderId, this.text, this.dim);
            }

            public String toString() {
                return "EmbeddingApi.EmbeddingRequest.Builder(model=" + this.model + ", folderId=" + this.folderId + ", text=" + this.text + ", dim=" + this.dim + ")";
            }
        }
    }

    @JsonPropertyOrder(
            {
                    "embedding",
                    "numTokens",
                    "modelVersion"
            }
    )
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingResponse(
            @JsonProperty("embedding") float[] embedding,
            @JsonProperty("numTokens") String numTokens,
            @JsonProperty("modelVersion") String modelVersion
    ) {}

}
