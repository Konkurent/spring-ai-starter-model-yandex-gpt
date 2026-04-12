package ru.ksoft.springaiyandexgpt.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import lombok.experimental.UtilityClass;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import ru.ksoft.springaiyandexgpt.api.HeadersProcessor;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.ClientProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.EmbeddingsProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.YandexAiProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.ClientPropertiesReducer;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.CommonConverter;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.FolderIdReducer;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingApi;
import ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingModel;
import ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configures {@link ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingApi} and
 * {@link ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingModel} for Spring AI embeddings with
 * provider {@code yandexai}.
 */
@AutoConfiguration(after = YandexAiAutoConfiguration.class)
@ConditionalOnProperty(
        name = SpringAiYandexGptModelProperties.EMBEDDING_MODEL,
        havingValue = SpringAiYandexGptModelProperties.YANDEX_AI,
        matchIfMissing = true
)
@ConditionalOnClass(YandexAiEmbeddingApi.class)
public class YandexAiEmbeddingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YandexAiEmbeddingApi yandexAiEmbeddingApi(
            YandexAiProperties commonProperties,
            EmbeddingsProperties embeddingsProperties,
            ObjectProvider<List<HeadersProcessor>> headersProcessorsProvider,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider
    ) {
        Assert.hasText(commonProperties.getBaseUrl(), "Missing base url. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".base-url'");
        Assert.hasText(embeddingsProperties.getEmbeddingPath(), "Missing embedding url. Please define either '" + SpringAiYandexGptModelProperties.YANDEX_AI_EMBEDDING_MODEL + ".embedding-path'");
        return new YandexAiEmbeddingApi(
                commonProperties.getBaseUrl(),
                embeddingsProperties.getEmbeddingPath(),
                headersProcessorsProvider.getIfAvailable(ArrayList::new),
                restClientBuilderProvider.getIfUnique(RestClient::builder)
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public YandexAiEmbeddingModel yandexAiEmbeddingModel(
            YandexAiProperties commonProperties,
            EmbeddingsProperties embeddingsProperties,
            YandexAiEmbeddingApi yandexAiEmbeddingApi,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<EmbeddingModelObservationConvention> observationConvention
    ) {
        String folderId = FolderIdReducer.reduce(commonProperties.getFolderId(), embeddingsProperties.getFolderId());
        Assert.hasText(folderId, "Missing Yandex Ai Chat folder id. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".folder-id' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_EMBEDDING_MODEL + ".folder-id' (chat-specific).");

        ClientProperties clientProperties = ClientPropertiesReducer.reduce(commonProperties.getClient(), embeddingsProperties.getClient());
        Assert.notNull(clientProperties, "Missing Yandex Ai Chat client configuration. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".client' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_EMBEDDING_MODEL + ".client' (chat-specific).");
        ClientSpec clientSpec = CommonConverter.toClientSpec(clientProperties);

        YandexAiEmbeddingModel embeddingModel = new YandexAiEmbeddingModel(
                yandexAiEmbeddingApi,
                embeddingsProperties.getMetadataMode(),
                Converter.toEmbeddingOptions(folderId, embeddingsProperties),
                clientSpec.retrySpec(),
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)
        );

        observationConvention.ifAvailable(embeddingModel::setObservationConvention);
        return embeddingModel;
    }

    @UtilityClass
    private static class Converter {

        public YandexAiEmbeddingOptions toEmbeddingOptions(String folderId, EmbeddingsProperties embeddingsProperties) {
            return YandexAiEmbeddingOptions.builder()
                    .folderId(folderId)
                    .dimensions(embeddingsProperties.getOptions().getDimensions())
                    .build();
        }

    }
}
