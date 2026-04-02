package ru.ksoft.springaiyandexgpt.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.ksoft.springaiyandexgpt.dto.Model;

@Getter
@RequiredArgsConstructor
public enum CompletionModel implements Model {
    ALICE_AI("aliceai-llm", "Alice AI LLM"),
    YANDEX_GPT_PRO_5_1("yandexgpt-5.1", "YandexGPT Pro 5.1"),
    YANDEX_GPT_PRO_5("yandexgpt-5-pro", "YandexGPT Pro 5"),
    YANDEX_GPT_LITE_5("yandexgpt-5-lite", "YandexGPT Lite 5");

    private final String name;
    private final String description;

    public String getUri(String... folder) {
        val folderId = folder[0];
        return "gpt://" + folderId + "/" + name;
    }

}
