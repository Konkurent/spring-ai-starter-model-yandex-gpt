package ru.kechlab.springaiyandexgpt.text;

import org.junit.jupiter.api.Test;
import ru.kechlab.springaiyandexgpt.constants.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;

class YandexAiChatOptionsTest {

    @Test
    void mergeCombinesStopSequencesAndHeaders() {
        YandexAiChatOptions base = YandexAiChatOptions.builder()
                .model(ChatModel.YANDEX_GPT_LITE_5)
                .folderId("f1")
                .stopSequences(java.util.List.of("a"))
                .build();

        YandexAiChatOptions overlay = YandexAiChatOptions.builder()
                .maxTokens(512)
                .stopSequences(java.util.List.of("b"))
                .build();

        YandexAiChatOptions merged = base.merge(overlay);

        assertThat(merged.getMaxTokens()).isEqualTo(512);
        assertThat(merged.getStopSequences()).containsExactly("a", "b");
    }

    @Test
    void copyProducesIndependentInstance() {
        YandexAiChatOptions opts = YandexAiChatOptions.builder()
                .model(ChatModel.ALICE_AI)
                .folderId("x")
                .build();
        YandexAiChatOptions copy = opts.copy();
        assertThat(copy.getFolderId()).isEqualTo("x");
        assertThat(copy.getChatModel()).isEqualTo(ChatModel.ALICE_AI);
    }
}
