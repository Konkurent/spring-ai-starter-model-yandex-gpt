package ru.kechlab.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.ai.document.MetadataMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.kechlab.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;

/**
 * Embedding model settings under {@code spring.ai.model.embedding.yandexai}: embeddings path,
 * {@link org.springframework.ai.document.MetadataMode} (default {@link MetadataMode#EMBED}), vector dimensions,
 * folder override, and client overrides.
 */
@Data
@ConfigurationProperties(prefix = EmbeddingsProperties.PREFIX)
public class EmbeddingsProperties {

    public static final String PREFIX = SpringAiYandexGptModelProperties.YANDEX_AI_EMBEDDING_MODEL;

    /** Default REST path for embeddings. */
    public static final String PATH = "/foundationModels/v1/embeddings";

    private String embeddingPath = PATH;

    private String folderId;

    private MetadataMode metadataMode = MetadataMode.EMBED;

    @NestedConfigurationProperty
    private Options options = new Options();

    @NestedConfigurationProperty
    private ClientProperties client;

    @Data
    public static class Options {
        private Integer dimensions = 256;
    }

}
