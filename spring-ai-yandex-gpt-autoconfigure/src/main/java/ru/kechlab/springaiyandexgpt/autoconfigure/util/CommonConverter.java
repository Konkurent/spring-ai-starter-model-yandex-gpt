package ru.kechlab.springaiyandexgpt.autoconfigure.util;

import lombok.experimental.UtilityClass;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.ClientProperties;
import ru.kechlab.springaiyandexgpt.dto.ClientSpec;

/**
 * Maps Boot {@link ClientProperties} to the core {@link ClientSpec} record used by models and APIs.
 */
@UtilityClass
public class CommonConverter {

    public static ClientSpec toClientSpec(ClientProperties clientProperties) {
        ClientSpec.RetrySpec retrySpec = new ClientSpec.RetrySpec(
                clientProperties.getRetry().getMaxAttempts(),
                clientProperties.getRetry().getMultiplier(),
                clientProperties.getRetry().getMinBackoff(),
                clientProperties.getRetry().getMaxBackoff()
        );
        return new ClientSpec(clientProperties.getTimeout(), retrySpec);
    }

}
