package ru.kechlab.springaiyandexgpt.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.model.ModelDescription;
import ru.kechlab.springaiyandexgpt.dto.ModelTemplate;

@Getter
@RequiredArgsConstructor
public enum ChatModel implements ModelTemplate, ModelDescription {
    ALICE_AI("aliceai-llm", "Alice AI LLM"),
    YANDEX_GPT_PRO_5_1("yandexgpt-5.1", "YandexGPT Pro 5.1"),
    YANDEX_GPT_PRO_5("yandexgpt-5-pro", "YandexGPT Pro 5"),
    YANDEX_GPT_LITE_5("yandexgpt-5-lite", "YandexGPT Lite 5"),
    YANDEX_GPT_LITE("yandexgpt-lite", "YandexGPT Lite") {
        @Override
        public String formatUri(String... args) {
            val suf = args[1];
            return super.formatUri(args) + "/latest@" + suf;
        }
    };

    private final String name;
    private final String description;

    public String formatUri(String... args) {
        val folderId = args[0];
        return "gpt://" + folderId + "/" + name;
    }

}
