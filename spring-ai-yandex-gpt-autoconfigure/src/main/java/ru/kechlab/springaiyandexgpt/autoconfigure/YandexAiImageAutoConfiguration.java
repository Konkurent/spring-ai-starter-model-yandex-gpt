package ru.kechlab.springaiyandexgpt.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import lombok.experimental.UtilityClass;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.kechlab.springaiyandexgpt.api.HttpHeadersProcessor;
import ru.kechlab.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.ClientProperties;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.ImageProperties;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.YandexAiProperties;
import ru.kechlab.springaiyandexgpt.autoconfigure.util.ClientPropertiesReducer;
import ru.kechlab.springaiyandexgpt.autoconfigure.util.CommonConverter;
import ru.kechlab.springaiyandexgpt.autoconfigure.util.FolderIdReducer;
import ru.kechlab.springaiyandexgpt.dto.ClientSpec;
import ru.kechlab.springaiyandexgpt.dto.ImageApi;
import ru.kechlab.springaiyandexgpt.image.YandexAiImageApi;
import ru.kechlab.springaiyandexgpt.image.YandexAiImageModel;
import ru.kechlab.springaiyandexgpt.image.YandexAiImageOptions;
import ru.kechlab.springaiyandexgpt.operation.CancelHandler;
import ru.kechlab.springaiyandexgpt.operation.OperationClient;
import ru.kechlab.springaiyandexgpt.operation.OperationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configures image generation: {@link YandexAiImageApi},
 * {@link ru.kechlab.springaiyandexgpt.operation.OperationService} for polling operations, and
 * {@link ru.kechlab.springaiyandexgpt.image.YandexAiImageModel}.
 */
@AutoConfiguration(after = YandexAiAutoConfiguration.class)
@ConditionalOnProperty(
        name = SpringAiYandexGptModelProperties.IMAGE_MODEL,
        havingValue = SpringAiYandexGptModelProperties.YANDEX_AI,
        matchIfMissing = true
)
@ConditionalOnClass(YandexAiImageApi.class)
public class YandexAiImageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YandexAiImageApi yandexAiImageApi(
            YandexAiProperties commonProperties,
            ImageProperties imageProperties,
            ObjectProvider<List<HttpHeadersProcessor>> headersProcessorsProvider,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider
    ) {
        Assert.hasText(commonProperties.getBaseUrl(), "Missing base url. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".base-url'");
        Assert.hasText(imageProperties.getImagePath(), "Missing image path. Please define either '" + SpringAiYandexGptModelProperties.YANDEX_AI_IMAGE_MODEL + ".image-path'");
        return new YandexAiImageApi(
                commonProperties.getBaseUrl(),
                imageProperties.getImagePath(),
                headersProcessorsProvider.getIfAvailable(ArrayList::new),
                restClientBuilderProvider.getIfUnique(RestClient::builder)
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationService<ImageApi.ImageResponseSpec> yandexAiImageOperationService(
            OperationClient operationClient,
            YandexAiProperties commonProperties,
            ImageProperties imageProperties,
            CancelHandler cancelHandler
    ) {
        ClientProperties clientProperties = ClientPropertiesReducer.reduce(commonProperties.getClient(), imageProperties.getClient());
        Assert.notNull(clientProperties, "Missing Yandex Ai Chat client configuration. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".client' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_IMAGE_MODEL + ".client' (chat-specific).");
        ClientSpec clientSpec = CommonConverter.toClientSpec(clientProperties);
        return new OperationService<>(
                clientSpec.retrySpec(),
                operationClient,
                cancelHandler
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public YandexAiImageModel yandexAiImageModel(
            YandexAiProperties commonProperties,
            ImageProperties imageProperties,
            YandexAiImageApi yandexAiImageApi,
            OperationService<ImageApi.ImageResponseSpec> imageOperationService,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ImageModelObservationConvention> observationConvention,
            ObjectProvider<Scheduler> schedulerProvide
    ) {
        ClientProperties clientProperties = ClientPropertiesReducer.reduce(commonProperties.getClient(), imageProperties.getClient());
        Assert.notNull(clientProperties, "Missing Yandex Ai Chat client configuration. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".client' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_IMAGE_MODEL + ".client' (chat-specific).");
        ClientSpec clientSpec = CommonConverter.toClientSpec(clientProperties);

        String folderId = FolderIdReducer.reduce(commonProperties.getFolderId(), imageProperties.getFolderId());
        Assert.hasText(folderId, "Missing Yandex Ai Chat folder id. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".folder-id' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_IMAGE_MODEL + ".folder-id' (chat-specific).");
        YandexAiImageOptions options = Converter.toImageOptions(folderId, imageProperties);
        YandexAiImageModel imageModel = new YandexAiImageModel(
                yandexAiImageApi,
                options,
                clientSpec.retrySpec(),
                observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP),
                schedulerProvide.getIfAvailable(Schedulers::boundedElastic),
                imageOperationService
        );

        observationConvention.ifAvailable(imageModel::setObservationConvention);

        return imageModel;
    }

    @UtilityClass
    private static class Converter {
        public YandexAiImageOptions toImageOptions(String folderId, ImageProperties imageProperties) {
            return YandexAiImageOptions.builder()
                    .folderId(folderId)
                    .mimeType(imageProperties.getOptions().getMimeType())
                    .seed(imageProperties.getOptions().getSeed())
                    .heightRatio(imageProperties.getOptions().getAspectRatio().getHeight())
                    .widthRatio(imageProperties.getOptions().getAspectRatio().getWidth())
                    .build();
        }

    }


}
