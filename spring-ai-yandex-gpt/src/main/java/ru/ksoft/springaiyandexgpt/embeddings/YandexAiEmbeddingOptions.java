package ru.ksoft.springaiyandexgpt.embeddings;

import lombok.Data;
import org.springframework.ai.embedding.EmbeddingOptions;

/**
 * Options for {@link YandexAiEmbeddingModel}: Yandex Cloud folder id and embedding vector size
 * ({@code dim} sent to the API).
 * <p>
 * {@link #getModel()} is not used for URI construction here; the model role (document vs query) is
 * chosen inside {@link YandexAiEmbeddingModel#buildRequest}.
 */
@Data
public class YandexAiEmbeddingOptions implements EmbeddingOptions {

    private String folderId;

    private Integer dimensions = 256;

    YandexAiEmbeddingOptions(String folderId, int dimensions) {
        this.folderId = folderId;
        this.dimensions = dimensions;
    }

    public YandexAiEmbeddingOptions.Builder mutate(YandexAiEmbeddingOptions other) {
        return new YandexAiEmbeddingOptions.Builder(other);
    }

    public YandexAiEmbeddingOptions merge(YandexAiEmbeddingOptions other) {
        if (other == null) return this;
        YandexAiEmbeddingOptions.Builder mutator = mutate(other);
        if (other.folderId != null) {
            mutator.folderId(other.folderId);
        }
        if (other.dimensions != null) {
            mutator.dimensions(other.dimensions);
        }
        return mutator.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getModel() {
        throw new UnsupportedOperationException("Model depends on embeddable data");
    }

    public static class Builder {
        private String folderId;
        private int dimensions;

        Builder() {
        }

        Builder(YandexAiEmbeddingOptions other) {
            this.folderId = other.folderId;
            this.dimensions = other.dimensions;
        }

        public Builder folderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder dimensions(int dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public YandexAiEmbeddingOptions build() {
            return new YandexAiEmbeddingOptions(this.folderId, this.dimensions);
        }

        public String toString() {
            return "YandexGptEmbeddingOptions.Builder(folderId=" + this.folderId + ", dimensions=" + this.dimensions + ")";
        }
    }
}
