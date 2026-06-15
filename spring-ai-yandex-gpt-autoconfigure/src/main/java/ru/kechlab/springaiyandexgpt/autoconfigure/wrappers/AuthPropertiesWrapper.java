package ru.kechlab.springaiyandexgpt.autoconfigure.wrappers;

import lombok.RequiredArgsConstructor;
import ru.kechlab.springaiyandexgpt.api.AuthOptions;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.AuthProperties;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Adapts {@link ru.kechlab.springaiyandexgpt.autoconfigure.properties.AuthProperties} to the core
 * {@link ru.kechlab.springaiyandexgpt.api.AuthOptions} interface, including IAM refresh interval in milliseconds.
 */
@RequiredArgsConstructor
public class AuthPropertiesWrapper implements AuthOptions {

    private final AuthProperties authProperties;

    @Override
    public String getApiKey() {
        return authProperties.getApiKey();
    }

    @Override
    public IamOptions getIam() {
        AuthProperties.IamProperties iamProperties = authProperties.getIam();
        return new IamOptions() {
            @Override
            public String getToken() {
                return iamProperties.getToken();
            }

            @Override
            public Path getTokenFile() {
                return iamProperties.getTokenFile();
            }

            @Override
            public Long getInterval() {
                return iamProperties.getRefreshInterval().get(getIntervalUnit().toChronoUnit());
            }

            @Override
            public TimeUnit getIntervalUnit() {
                return TimeUnit.MILLISECONDS;
            }
        };
    }
}
