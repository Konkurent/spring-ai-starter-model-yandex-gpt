package ru.ksoft.springaiyandexgpt.autoconfigure;

import io.micrometer.observation.ObservationRegistry;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ksoft.springaiyandexgpt.api.HeadersProcessor;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.ChatProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.ClientProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.YandexAiProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.ClientPropertiesReducer;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.CommonConverter;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.FolderIdReducer;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatApi;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatModel;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configures {@link ru.ksoft.springaiyandexgpt.text.YandexAiChatApi} and
 * {@link ru.ksoft.springaiyandexgpt.text.YandexAiChatModel} when Spring AI's chat model provider is
 * set to Yandex ({@code spring.ai.model.chat=yandexai} by default).
 */
@AutoConfiguration(after = YandexAiAutoConfiguration.class)
@ConditionalOnProperty(
        name = SpringAiYandexGptModelProperties.CHAT_MODEL,
        havingValue = SpringAiYandexGptModelProperties.YANDEX_AI,
        matchIfMissing = true
)
@ConditionalOnClass(YandexAiChatApi.class)
public class YandexAiChatAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public YandexAiChatApi yandexAiChatApi(
            YandexAiProperties commonProperties,
            ChatProperties chatProperties,
            ObjectProvider<List<HeadersProcessor>> headersProcessorsProvider,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider,
            ObjectProvider<WebClient.Builder> webClientBuilderProvider
    ) {
        Assert.hasText(commonProperties.getBaseUrl(), "Base url must not be null or empty");
        Assert.hasText(chatProperties.getCompletionPath(), "Completion path must not be null or empty");
        return new YandexAiChatApi(
                commonProperties.getBaseUrl(),
                chatProperties.getCompletionPath(),
                headersProcessorsProvider.getIfAvailable(ArrayList::new),
                restClientBuilderProvider.getIfUnique(RestClient::builder),
                webClientBuilderProvider.getIfUnique(WebClient::builder)
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public YandexAiChatModel yandexAiChatModel(YandexAiChatApi chatApi,
                                               YandexAiProperties commonProperties,
                                               ChatProperties chatProperties,
                                               ObjectProvider<ObservationRegistry> observationRegistryProvider
    ) {
        ClientProperties clientProperties = ClientPropertiesReducer.reduce(commonProperties.getClient(), chatProperties.getClient());
        Assert.notNull(clientProperties, "Missing Yandex Ai Chat client configuration. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".client' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_CHAT_MODEL + ".client' (chat-specific).");
        ClientSpec clientSpec = CommonConverter.toClientSpec(clientProperties);

        String folderId = FolderIdReducer.reduce(commonProperties.getFolderId(), chatProperties.getFolderId());
        Assert.hasText(folderId, "Missing Yandex Ai Chat folder id. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".folder-id' (common) or '" + SpringAiYandexGptModelProperties.YANDEX_AI_CHAT_MODEL + ".folder-id' (chat-specific).");
        YandexAiChatOptions options = Converter.toChatOptions(folderId, chatProperties);
        return new YandexAiChatModel(
                chatApi,
                options,
                clientSpec.retrySpec(),
                observationRegistryProvider.getIfUnique(() -> ObservationRegistry.NOOP)
        );
    }

    @UtilityClass
    private static class Converter {

        public static YandexAiChatOptions toChatOptions(String folderId, ChatProperties chatProperties) {
            ChatProperties.Options options = chatProperties.getOptions();
            return YandexAiChatOptions.builder()
                    .folderId(folderId)
                    .model(chatProperties.getModel())
                    .temperature(options.getTemperature())
                    .maxTokens(options.getMaxTokens())
                    .reasoningMode(options.getReasoningMode())
                    .build();
        }

    }
}
