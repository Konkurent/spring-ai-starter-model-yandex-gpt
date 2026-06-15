package ru.kechlab.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;

/**
 * Sets {@code User-Agent: spring-ai}. With auto-configuration this class is registered as a bean;
 * for manual setup, add it to your list of {@link HttpHeadersProcessor} beans.
 */
public class UserAgentHeadersProcessor implements HttpHeadersProcessor {

    public static final String HTTP_USER_AGENT_HEADER = "User-Agent";

    public static final String SPRING_AI_USER_AGENT = "spring-ai";

    @Override
    public void process(HttpHeaders headers) {
        headers.set(HTTP_USER_AGENT_HEADER, SPRING_AI_USER_AGENT);
    }
}
