package ru.ksoft.springaiyandexgpt.dto;

import org.junit.jupiter.api.Test;
import org.springframework.ai.image.Image;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncImageTest {

    @Test
    void getB64JsonBlocksUntilMonoCompletes() {
        Mono<Image> mono = Mono.just(new Image(null, "QUJD"));
        AsyncImage asyncImage = new AsyncImage("op-42", mono);
        assertThat(asyncImage.getOperationId()).isEqualTo("op-42");
        assertThat(asyncImage.getB64Json()).isEqualTo("QUJD");
    }

    @Test
    void asMonoExposesReactivePipeline() {
        AsyncImage asyncImage = new AsyncImage("op", Mono.just(new Image(null, "QQ==")));
        assertThat(asyncImage.asMono().block().getB64Json()).isEqualTo("QQ==");
    }
}
