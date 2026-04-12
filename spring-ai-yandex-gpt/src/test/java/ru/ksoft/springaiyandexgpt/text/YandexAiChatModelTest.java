package ru.ksoft.springaiyandexgpt.text;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import ru.ksoft.springaiyandexgpt.constants.CompletionModel;
import ru.ksoft.springaiyandexgpt.constants.CompletionResponseStatus;
import ru.ksoft.springaiyandexgpt.constants.Role;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.Alternative;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ChatCompletionResponse;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ContentUsage;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.ResultHolder;
import ru.ksoft.springaiyandexgpt.dto.ChatApi.TextualMessage;
import ru.ksoft.springaiyandexgpt.dto.ClientSpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YandexAiChatModelTest {

    @Test
    void callMapsCompletionResponseToChatResponse() {
        YandexAiChatApi chatApi = mock(YandexAiChatApi.class);
        YandexAiChatOptions options = YandexAiChatOptions.builder()
                .model(CompletionModel.YANDEX_GPT_PRO_5_1)
                .folderId("b1gfolder")
                .build();

        ChatCompletionResponse apiBody = ChatCompletionResponse.builder()
                .alternatives(java.util.List.of(
                        new Alternative(
                                TextualMessage.builder().role(Role.ASSISTANT).text("Reply").build(),
                                CompletionResponseStatus.ALTERNATIVE_STATUS_FINAL
                        )
                ))
                .usage(new ContentUsage("1", "2", "3", null))
                .modelVersion("2025.01")
                .build();

        when(chatApi.completionEntity(any())).thenReturn(ResponseEntity.ok(new ResultHolder<>(apiBody)));

        ClientSpec.RetrySpec retrySpec = new ClientSpec.RetrySpec(1, 1.0, 1L, 10L);
        YandexAiChatModel model = new YandexAiChatModel(chatApi, options, retrySpec, ObservationRegistry.NOOP);

        ChatResponse response = model.call(new Prompt(new UserMessage("Hello"), options));

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getOutput().getText()).isEqualTo("Reply");
        ChatResponseMetadata meta = response.getMetadata();
        assertThat((Object) meta.get("modelVersion")).isEqualTo("2025.01");
    }

    @Test
    void callUsesCompletionEntityWithoutExtraHeadersWhenOptionsHaveNoHttpHeaders() {
        YandexAiChatApi chatApi = mock(YandexAiChatApi.class);
        YandexAiChatOptions options = YandexAiChatOptions.builder()
                .model(CompletionModel.YANDEX_GPT_LITE_5)
                .folderId("f")
                .build();

        ChatCompletionResponse apiBody = ChatCompletionResponse.builder()
                .alternatives(java.util.List.of(
                        new Alternative(
                                TextualMessage.builder().role(Role.ASSISTANT).text("x").build(),
                                CompletionResponseStatus.ALTERNATIVE_STATUS_FINAL
                        )
                ))
                .build();

        when(chatApi.completionEntity(any())).thenReturn(ResponseEntity.ok(new ResultHolder<>(apiBody)));

        YandexAiChatModel model = new YandexAiChatModel(
                chatApi,
                options,
                new ClientSpec.RetrySpec(1, 1.0, 1L, 10L),
                ObservationRegistry.NOOP
        );

        model.call(new Prompt(new UserMessage("a"), options));

        org.mockito.Mockito.verify(chatApi).completionEntity(any());
        org.mockito.Mockito.verify(chatApi, org.mockito.Mockito.never()).completionEntity(any(), any(HttpHeaders.class));
    }
}
