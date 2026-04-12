package ru.ksoft.springaiyandexgpt.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class HeadersProcessorsTest {

    @Test
    void apiKeyAuthorizationAddsApiKeyPrefix() {
        HttpHeaders headers = new HttpHeaders();
        new ApiKeyAuthorizationProcessor("secret").process(headers);
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(ApiKeyAuthorizationProcessor.PREFIX + " secret");
    }

    @Test
    void userAgentHeadersProcessorSetsSpringAiUserAgent() {
        HttpHeaders headers = new HttpHeaders();
        new UserAgentHeadersProcessor().process(headers);
        assertThat(headers.getFirst(UserAgentHeadersProcessor.HTTP_USER_AGENT_HEADER))
                .isEqualTo(UserAgentHeadersProcessor.SPRING_AI_USER_AGENT);
    }

    @Test
    void iamTokenAuthorizationProcessorUsesBearerFromStaticToken() {
        AuthOptions.IamOptions iam = new AuthOptions.IamOptions();
        iam.setToken("static-iam-token");
        HttpHeaders headers = new HttpHeaders();
        IamTokenAuthorizationProcessor processor = new IamTokenAuthorizationProcessor(iam);
        processor.process(headers);
        processor.destroy();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer static-iam-token");
    }
}
