package ru.kechlab.springaiyandexgpt.exceptions;

import org.junit.jupiter.api.Test;
import ru.kechlab.springaiyandexgpt.constants.ErrorCode;
import ru.kechlab.springaiyandexgpt.dto.OperationApi;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YandexOperationFailedExceptionTest {

    @Test
    void carriesMessageAndErrorCodeFromOperationError() {
        var error = new OperationApi.Operation.Error(ErrorCode.INVALID_ARGUMENT, "bad", Map.of("k", "v"));
        YandexOperationFailedException ex = new YandexOperationFailedException(error);
        assertThat(ex).hasMessage("bad");
        assertThat(ex.getCause()).isNull();
    }
}
