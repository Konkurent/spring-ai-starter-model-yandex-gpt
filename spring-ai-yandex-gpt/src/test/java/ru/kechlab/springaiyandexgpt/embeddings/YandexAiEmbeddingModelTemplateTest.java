package ru.kechlab.springaiyandexgpt.embeddings;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import ru.kechlab.springaiyandexgpt.constants.EmbeddingModel;
import ru.kechlab.springaiyandexgpt.dto.ClientSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YandexAiEmbeddingModelTemplateTest {

    @Test
    void callReturnsEmbeddingFromApiResponse() {
        YandexAiEmbeddingApi api = mock(YandexAiEmbeddingApi.class);
        float[] vector = new float[] { 0.1f, 0.2f };
        when(api.embedding(any(), any())).thenReturn(
                ResponseEntity.ok(new YandexAiEmbeddingApi.EmbeddingResponse(vector, "10", "v1"))
        );

        YandexAiEmbeddingOptions options = YandexAiEmbeddingOptions.builder()
                .folderId("fld")
                .dimensions(2)
                .build();

        RetryTemplate retry = new RetryTemplate();
        YandexAiEmbeddingModel model = new YandexAiEmbeddingModel(
                api,
                MetadataMode.NONE,
                options,
                new ClientSpec.RetrySpec(1, 1.0, 100L, 100L),
                ObservationRegistry.NOOP
        );

        EmbeddingRequest request = new EmbeddingRequest(java.util.List.of("text"), null);
        EmbeddingResponse response = model.call(request, EmbeddingModel.DOC);

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResult().getOutput()).containsExactly(0.1f, 0.2f);
    }
}
