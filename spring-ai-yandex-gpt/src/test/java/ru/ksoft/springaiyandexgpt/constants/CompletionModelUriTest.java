package ru.ksoft.springaiyandexgpt.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionModelUriTest {

    @Test
    void completionModelUriFollowsGptScheme() {
        assertThat(CompletionModel.YANDEX_GPT_PRO_5_1.getUri("b1gfolder"))
                .isEqualTo("gpt://b1gfolder/yandexgpt-5.1");
    }
}
