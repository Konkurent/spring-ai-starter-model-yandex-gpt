package ru.ksoft.springaiyandexgpt.exceptions;

import org.junit.jupiter.api.Test;
import ru.ksoft.springaiyandexgpt.constants.ErrorCode;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;

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
