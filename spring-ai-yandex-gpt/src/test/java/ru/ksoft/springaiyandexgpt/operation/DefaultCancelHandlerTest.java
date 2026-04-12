package ru.ksoft.springaiyandexgpt.operation;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.ksoft.springaiyandexgpt.dto.OperationApi;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCancelHandlerTest {

    @Test
    void cancelInvokesClientWithCancelPathResolver() {
        OperationClient client = mock(OperationClient.class);
        when(client.cancel(argThat(spec -> true))).thenReturn(ResponseEntity.noContent().build());

        RetryTemplate retry = new RetryTemplate();
        retry.setRetryPolicy(new SimpleRetryPolicy(1));

        DefaultCancelHandler handler = new DefaultCancelHandler(retry, client);
        handler.cancel("operations/op-1", OperationApi.PathResolver.Default.CANCEL);

        verify(client).cancel(argThat(spec ->
                "operations/op-1".equals(spec.id())
                        && "operations/op-1:cancel".equals(spec.pathResolver().resolve(spec.id()))));
    }
}
