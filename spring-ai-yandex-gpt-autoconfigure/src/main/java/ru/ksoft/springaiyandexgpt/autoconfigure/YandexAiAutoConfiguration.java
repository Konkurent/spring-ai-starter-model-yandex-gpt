package ru.ksoft.springaiyandexgpt.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ksoft.springaiyandexgpt.api.HeadersProcessor;
import ru.ksoft.springaiyandexgpt.api.UserAgentHeadersProcessor;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.*;
import ru.ksoft.springaiyandexgpt.autoconfigure.util.CommonConverter;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;
import ru.ksoft.springaiyandexgpt.operation.CancelHandler;
import ru.ksoft.springaiyandexgpt.operation.DefaultCancelHandler;
import ru.ksoft.springaiyandexgpt.operation.OperationClient;
import ru.ksoft.springaiyandexgpt.operation.OperationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Base auto-configuration for Yandex AI: registers a default {@link ru.ksoft.springaiyandexgpt.api.UserAgentHeadersProcessor},
 * the shared {@link ru.ksoft.springaiyandexgpt.operation.OperationClient} for long-running operations, and a
 * {@link ru.ksoft.springaiyandexgpt.operation.DefaultCancelHandler} when an {@link ru.ksoft.springaiyandexgpt.operation.OperationService}
 * bean exists.
 */
@AutoConfiguration(
        after = {
                RestClientAutoConfiguration.class,
                WebClientAutoConfiguration.class
        }
)
@EnableConfigurationProperties(
        {
                YandexAiProperties.class,
                AuthProperties.class,
                ChatProperties.class,
                EmbeddingsProperties.class,
                ImageProperties.class
        }
)
public class YandexAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserAgentHeadersProcessor userAgentHeadersProcessor() {
        return new UserAgentHeadersProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationClient yandexAiOperationClient(
            YandexAiProperties commonProperties,
            ObjectProvider<List<HeadersProcessor>> headersProcessors,
            ObjectProvider<RestClient.Builder> restClientBuilder,
            ObjectProvider<WebClient.Builder> webclientBuilder
    ) {
        Assert.hasText(commonProperties.getBaseUrl(), "Missing base url. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".base-url'");
        Assert.hasText(commonProperties.getOperationPath(), "Missing operation path. Please define either '" + SpringAiYandexGptModelProperties.MODEL_PREFIX + ".operation-path'");
        return new OperationClient(
                commonProperties.getBaseUrl(),
                commonProperties.getOperationPath(),
                headersProcessors.getIfAvailable(ArrayList::new),
                webclientBuilder.getIfAvailable(WebClient::builder),
                restClientBuilder.getIfAvailable(RestClient::builder)
        );
    }

    @Bean
    @ConditionalOnBean(OperationService.class)
    @ConditionalOnMissingBean(CancelHandler.class)
    public DefaultCancelHandler defaultCancelHandler(OperationClient operationClient) {
        return new DefaultCancelHandler(operationClient);
    }

}
