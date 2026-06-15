package ru.kechlab.springaiyandexgpt.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.kechlab.springaiyandexgpt.constants.EmbeddingModel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Embeddings: preset {@code modelUri} values are built from {@link EmbeddingModel} ({@code emb://...} templates).
 */
class EmbeddingApiSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void embeddingRequestSpecSerializesDocModelUri() throws Exception {
        String folder = "b1gtest";
        EmbeddingApi.EmbeddingRequestSpec spec = EmbeddingApi.EmbeddingRequestSpec.builder()
                .model(EmbeddingModel.DOC)
                .folderId(folder)
                .text("hello")
                .dim("256")
                .build();

        String json = mapper.writeValueAsString(spec);

        assertThat(json).contains("\"modelUri\":\"emb://" + folder + "/text-search-doc/latest\"");
        assertThat(json).contains("\"text\":\"hello\"");
        assertThat(json).contains("\"dim\":\"256\"");
    }
}
