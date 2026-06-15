package ru.kechlab.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;

import java.util.UUID;

public class XClientRequestIdProcessor implements HttpHeadersProcessor {

    public static final String X_CLIENT_REQUEST_ID = "x-client-request-id";

    @Override
    public void process(HttpHeaders headers) {
        headers.add(X_CLIENT_REQUEST_ID, UUID.randomUUID().toString());
    }
}
