package ru.kechlab.springaiyandexgpt.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatModelTemplateUriTest {

    @Test
    void completionModelUriFollowsGptScheme() {
        assertThat(ChatModel.YANDEX_GPT_PRO_5_1.formatUri("b1gfolder"))
                .isEqualTo("gpt://b1gfolder/yandexgpt-5.1");
    }
}
