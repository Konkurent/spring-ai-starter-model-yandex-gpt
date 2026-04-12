package ru.ksoft.springaiyandexgpt.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import ru.ksoft.springaiyandexgpt.api.ApiKeyAuthorizationProcessor;
import ru.ksoft.springaiyandexgpt.api.AuthorizationProcessor;
import ru.ksoft.springaiyandexgpt.api.IamTokenAuthorizationProcessor;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.properties.AuthProperties;
import ru.ksoft.springaiyandexgpt.autoconfigure.wrappers.AuthPropertiesWrapper;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatApi;

/**
 * Registers a single {@link ru.ksoft.springaiyandexgpt.api.AuthorizationProcessor} bean from
 * {@link ru.ksoft.springaiyandexgpt.autoconfigure.properties.AuthProperties}: IAM token (optionally
 * refreshed from a file) or API key.
 */
@AutoConfiguration(after = YandexAiAutoConfiguration.class)
@ConditionalOnProperty(
        name = SpringAiYandexGptModelProperties.AUTH,
        havingValue = SpringAiYandexGptModelProperties.YANDEX_AI,
        matchIfMissing = true
)
@ConditionalOnClass(YandexAiChatApi.class)
public class YandexAiAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationProcessor authorizationProcessor(AuthProperties authProperties) {
        if (authProperties == null) throw  new IllegalArgumentException("authProperties cannot be null");
        AuthProperties.IamProperties iamProperties = authProperties.getIam();
        if (iamProperties != null && (iamProperties.getToken() != null || iamProperties.getTokenFile() != null)) {
            return new IamTokenAuthorizationProcessor(new AuthPropertiesWrapper(authProperties).getIam());
        }
        if (iamProperties != null && authProperties.getApiKey() != null) {
            return new ApiKeyAuthorizationProcessor(authProperties.getApiKey());
        }
        throw new IllegalStateException("Authentication not configured: either 'apiKey' or 'iam.token' / 'iam.tokenFile' must be provided");
    }

}
