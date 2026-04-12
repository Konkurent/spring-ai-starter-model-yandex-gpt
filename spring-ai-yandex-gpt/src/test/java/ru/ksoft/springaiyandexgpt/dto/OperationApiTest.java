package ru.ksoft.springaiyandexgpt.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationApiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void operationDeserializationSupportsLongRunningOperationShape() throws Exception {
        String json = """
                {
                  "id": "op-1",
                  "done": true,
                  "response": { "image": "YmFzZTY0" }
                }
                """;

        OperationApi.Operation<ImageApi.ImageResponseSpec> op = mapper.readValue(
                json,
                mapper.getTypeFactory().constructParametricType(OperationApi.Operation.class, ImageApi.ImageResponseSpec.class)
        );

        assertThat(op.id()).isEqualTo("op-1");
        assertThat(op.done()).isTrue();
        assertThat(op.responseSpec().image()).isEqualTo("YmFzZTY0");
    }
}
