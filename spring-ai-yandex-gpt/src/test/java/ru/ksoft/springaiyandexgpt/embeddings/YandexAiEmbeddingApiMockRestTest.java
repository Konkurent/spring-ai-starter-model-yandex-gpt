package ru.ksoft.springaiyandexgpt.embeddings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

/**
 * HTTP layer test for embeddings: JSON body with {@code modelUri}, {@code text}, and {@code dim}
 * per the Foundation Models contract.
 */
class YandexAiEmbeddingApiMockRestTest {

    private MockRestServiceServer server;
    private RestClient.Builder builder;
    private YandexAiEmbeddingApi api;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        api = new YandexAiEmbeddingApi(
                "https://llm.example",
                "/foundationModels/v1/textEmbedding",
                List.of(),
                builder
        );
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void embeddingPostsJsonBodyAndParsesResponse() {
        server.expect(requestTo("https://llm.example/foundationModels/v1/textEmbedding"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {"modelUri":"emb://fld/text-search-doc/latest","text":"hello","dim":"256"}
                        """))
                .andRespond(withSuccess(
                        """
                                {"embedding":[0.25,0.75],"numTokens":"2","modelVersion":"2025.01"}
                                """,
                        MediaType.APPLICATION_JSON
                ));

        YandexAiEmbeddingApi.EmbeddingRequest req = new YandexAiEmbeddingApi.EmbeddingRequest(
                "emb://fld/text-search-doc/latest",
                "hello",
                "256"
        );
        ResponseEntity<YandexAiEmbeddingApi.EmbeddingResponse> entity = api.embedding(req, new HttpHeaders());

        assertThat(entity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().embedding()).containsExactly(0.25f, 0.75f);
        assertThat(entity.getBody().modelVersion()).isEqualTo("2025.01");
    }
}
