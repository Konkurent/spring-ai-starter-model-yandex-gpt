package ru.ksoft.springaiyandexgpt.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.ksoft.springaiyandexgpt.constants.CompletionModel;
import ru.ksoft.springaiyandexgpt.constants.ReasoningMode;
import ru.ksoft.springaiyandexgpt.text.YandexAiChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Request body shape matches the model URI pattern {@code gpt://&lt;folder&gt;/&lt;model&gt;}
 * (see <a href="https://aistudio.yandex.ru/docs/ru/ai-studio/concepts/api.html">AI Studio / Foundation Models documentation</a>).
 */
class ChatApiSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void chatCompletionRequestSerializesModelUriInGptScheme() throws Exception {
        String folderId = "b1gtestfolder";
        ChatApi.ChatCompletionRequest request = ChatApi.ChatCompletionRequest.builder()
                .model(CompletionModel.YANDEX_GPT_PRO_5_1)
                .folderId(folderId)
                .completionOptions(
                        ChatApi.YandexGptCompletionOptions.builder()
                                .stream(false)
                                .temperature(0.3f)
                                .maxTokens(64)
                                .withReasoningMode(ReasoningMode.DISABLED)
                                .build()
                )
                .messages(java.util.List.of(
                        ChatApi.TextualMessage.builder().role(ru.ksoft.springaiyandexgpt.constants.Role.USER).text("ping").build()
                ))
                .build();

        String json = mapper.writeValueAsString(request);

        assertThat(json).contains("\"modelUri\":\"gpt://" + folderId + "/" + CompletionModel.YANDEX_GPT_PRO_5_1.getName() + "\"");
        assertThat(json).contains("\"text\":\"ping\"");
    }

    @Test
    void yandexGptCompletionOptionsFromChatOptionsCopiesParameters() {
        YandexAiChatOptions opts = YandexAiChatOptions.builder()
                .model(CompletionModel.YANDEX_GPT_LITE_5)
                .folderId("f1")
                .temperature(0.5f)
                .maxTokens(100)
                .reasoningMode(ReasoningMode.ENABLED_HIDDEN)
                .build();

        ChatApi.YandexGptCompletionOptions co = ChatApi.YandexGptCompletionOptions.builder()
                .stream(false)
                .temperature(opts.getTemperature() != null ? opts.getTemperature().floatValue() : null)
                .maxTokens(opts.getMaxTokens())
                .reasoningOptions(new ChatApi.ReasoningOptions(opts.getReasoningMode()))
                .build();

        assertThat(co.stream()).isFalse();
        assertThat(co.temperature()).isEqualTo(0.5f);
        assertThat(co.maxTokens()).isEqualTo(100);
        assertThat(co.reasoningOptions().mode()).isEqualTo(ReasoningMode.ENABLED_HIDDEN);
    }
}
