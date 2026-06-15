package ru.kechlab.springaiyandexgpt.api;

/**
 * Adds an {@code Authorization} header for Yandex Cloud / Foundation Models API calls.
 * <p>
 * Typical implementations use an API key ({@code Api-Key ...}) or an IAM token ({@code Bearer ...}).
 */
public interface AuthorizationProcessor extends HttpHeadersProcessor {

    /** Standard HTTP header name for credentials. */
    String AUTHORIZATION = "Authorization";

}
