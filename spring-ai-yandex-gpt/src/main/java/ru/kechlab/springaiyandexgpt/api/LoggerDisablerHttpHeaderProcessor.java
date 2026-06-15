package ru.kechlab.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;

public class LoggerDisablerHttpHeaderProcessor implements HttpHeadersProcessor {

    public static final String X_DATA_LOGGING_ENABLED = "x-data-logging-enabled";

    @Override
    public void process(HttpHeaders headers) {
        headers.add(X_DATA_LOGGING_ENABLED, Boolean.FALSE.toString());
    }
}
