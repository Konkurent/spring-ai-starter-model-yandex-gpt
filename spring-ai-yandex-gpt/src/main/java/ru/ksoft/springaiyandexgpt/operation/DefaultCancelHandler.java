package ru.ksoft.springaiyandexgpt.operation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;
import ru.ksoft.springaiyandexgpt.utils.RetryExecutor;

/**
 * Default {@link CancelHandler} that forwards to {@link OperationClient#cancel}.
 */
@Slf4j
public class DefaultCancelHandler implements CancelHandler {


    private final OperationClient client;

    public DefaultCancelHandler(OperationClient client) {
        Assert.notNull(client, "Client must not be null");
        this.client = client;
    }

    @Override
    public void cancel(String operationId, OperationApi.PathResolver cancelPathResolver) {
        client.cancel(new OperationApi.OperationRequestSpec(operationId, cancelPathResolver));
    }

}
