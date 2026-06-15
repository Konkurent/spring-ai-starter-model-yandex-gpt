package ru.kechlab.springaiyandexgpt.operation;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.kechlab.springaiyandexgpt.dto.OperationApi;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCancelHandlerTest {

    @Test
    void cancelInvokesClientWithCancelPathResolver() {
        OperationClient client = mock(OperationClient.class);
        when(client.cancel(argThat(spec -> true))).thenReturn(ResponseEntity.noContent().build());

        DefaultCancelHandler handler = new DefaultCancelHandler(client);
        handler.cancel("operations/op-1", OperationApi.PathResolver.Default.CANCEL);

        verify(client).cancel(argThat(spec ->
                "operations/op-1".equals(spec.id())
                        && "operations/op-1:cancel".equals(spec.pathResolver().resolve(spec.id()))));
    }
}
