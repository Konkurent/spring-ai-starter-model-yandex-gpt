package ru.kechlab.springaiyandexgpt.api;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Credentials for calling Yandex Foundation Models: either an API key or IAM-based authentication.
 * <p>
 * The auto-configuration module maps {@link ru.kechlab.springaiyandexgpt.autoconfigure.properties.AuthProperties}
 * to this shape via {@link ru.kechlab.springaiyandexgpt.autoconfigure.wrappers.AuthPropertiesWrapper}.
 */
public interface AuthOptions {

    /** API key when using {@code Api-Key} authentication; may be {@code null} if IAM is used. */
    String getApiKey();

    /** IAM settings; may be {@code null} if only an API key is used. */
    IamOptions getIam();

    /**
     * IAM token source: inline value and/or file on disk, plus how often to re-read the file.
     */
    interface IamOptions {

        /** Current token string; may be {@code null} if loaded only from {@link #getTokenFile()}. */
        String getToken();

        /** File whose content is the IAM token; optional if {@link #getToken()} is set. */
        Path getTokenFile();

        /** Refresh period length in {@link #getIntervalUnit()}. */
        Long getInterval();

        /** Unit for {@link #getInterval()}. */
        TimeUnit getIntervalUnit();
    }
}
