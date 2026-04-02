package ru.ksoft.springaiyandexgpt.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnMissingBean(UserAgentHeaderProcessor.class)
public class UserAgentHeaderProcessor implements HeaderProcessor {

    public static final String HTTP_USER_AGENT_HEADER = "User-Agent";

    public static final String SPRING_AI_USER_AGENT = "spring-ai";

    @Override
    public void process(HttpHeaders headers) {
        headers.set(HTTP_USER_AGENT_HEADER, SPRING_AI_USER_AGENT);
    }
}
