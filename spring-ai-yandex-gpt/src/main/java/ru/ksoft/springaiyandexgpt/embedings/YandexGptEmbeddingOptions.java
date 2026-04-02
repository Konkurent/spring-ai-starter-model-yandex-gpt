package ru.ksoft.springaiyandexgpt.embedings;

import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.embedding.EmbeddingOptions;

@Data
public class YandexGptEmbeddingOptions implements EmbeddingOptions {

    private String folderId;

    private Integer dimensions = 256;

    YandexGptEmbeddingOptions(String folderId, int dimensions) {
        this.folderId = folderId;
        this.dimensions = dimensions;
    }

    public YandexGptEmbeddingOptions.Builder mutate(YandexGptEmbeddingOptions other) {
        return new YandexGptEmbeddingOptions.Builder(other);
    }

    public YandexGptEmbeddingOptions merge(YandexGptEmbeddingOptions other) {
        if (other == null) return this;
        YandexGptEmbeddingOptions.Builder mutator = mutate(other);
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
    public @Nullable String getModel() {
        throw new UnsupportedOperationException("Model depends on embeddable data");
    }

    public static class Builder {
        private String folderId;
        private int dimensions;

        Builder() {
        }

        Builder(YandexGptEmbeddingOptions other) {
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

        public YandexGptEmbeddingOptions build() {
            return new YandexGptEmbeddingOptions(this.folderId, this.dimensions);
        }

        public String toString() {
            return "YandexGptEmbeddingOptions.Builder(folderId=" + this.folderId + ", dimensions=" + this.dimensions + ")";
        }
    }
}
