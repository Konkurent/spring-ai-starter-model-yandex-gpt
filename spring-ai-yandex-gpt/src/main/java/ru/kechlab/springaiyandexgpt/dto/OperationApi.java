package ru.kechlab.springaiyandexgpt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import ru.kechlab.springaiyandexgpt.constants.ErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTOs for Yandex Cloud <a href="https://yandex.cloud/en/docs/api-design-guide/concepts/operation">operations</a>:
 * status/cancel requests and {@link Operation} response bodies.
 */
public class OperationApi {

    /** HTTP request parameters for the operations API: id, path resolution, and headers. */
    public record OperationRequestSpec(
            String id,
            PathResolver pathResolver,
            HttpHeaders headers
    ) {

        public OperationRequestSpec(String id, PathResolver pathResolver) {
            this(id, pathResolver, new HttpHeaders());
        }
    }

    /** Resolves a path segment or full path to the operation resource from its id. */
    public interface PathResolver {

        /** Built-in resolvers: GET and cancel ({@code :cancel}). */
        class Default {

            public static final PathResolver GET = operationId -> operationId;
            public static final PathResolver CANCEL = operationId -> operationId + ":cancel";

        }

        String resolve(String operationId);
    }


    /**
     * Generic Yandex Cloud operation body: metadata, completion flag, error, and typed {@code response}.
     *
     * @param <T> type of the {@code response} field (API-specific)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Operation<T>(
            @JsonProperty("id") String id,
            @JsonProperty("description") String description,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("modifiedAt") String modifiedAt,
            @JsonProperty("done") Boolean done,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("error") Error error,
            @JsonProperty("response") T responseSpec
    ) {

        /** Operation error payload ({@link ErrorCode}, message, details). */
        public record Error(
                @JsonProperty("code") ErrorCode code,
                @JsonProperty("message") String message,
                @JsonProperty("details") Map<String, Object> details
        ) {

        }


    }



}