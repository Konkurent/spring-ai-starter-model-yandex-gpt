package ru.kechlab.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * Sends a static API key in the {@code Authorization} header using the {@code Api-Key} scheme
 * expected by Yandex Cloud.
 */
public class ApiKeyAuthorizationProcessor implements AuthorizationProcessor {

    private final String apiKey;

    /** Prefix before the secret value in the {@code Authorization} header value. */
    public static String PREFIX = "Api-Key";

    /**
     * @param apiKey secret API key; must not be {@code null}
     */
    public ApiKeyAuthorizationProcessor(String apiKey) {
        Assert.notNull(apiKey, "Api key must not be null");
        this.apiKey = apiKey;
    }

    @Override
    public void process(HttpHeaders headers) {
        headers.add(AuthorizationProcessor.AUTHORIZATION, PREFIX + " " + apiKey);
    }

}
