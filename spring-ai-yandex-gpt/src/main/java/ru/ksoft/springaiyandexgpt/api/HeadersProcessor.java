package ru.ksoft.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;

/**
 * Adds or updates HTTP headers on outgoing Yandex AI requests.
 * <p>
 * Implementations are composed into a list (for example by auto-configuration) and run in order
 * before each REST or WebClient call.
 */
public interface HeadersProcessor {

    /**
     * Mutates the given headers in place (add, set, or remove values).
     *
     * @param headers headers that will be sent with the request; never {@code null}
     */
    void process(HttpHeaders headers);

}
