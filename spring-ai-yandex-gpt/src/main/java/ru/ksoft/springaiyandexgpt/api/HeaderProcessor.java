package ru.ksoft.springaiyandexgpt.api;

import org.springframework.http.HttpHeaders;

public interface HeaderProcessor {
    void process(HttpHeaders headers);

}
