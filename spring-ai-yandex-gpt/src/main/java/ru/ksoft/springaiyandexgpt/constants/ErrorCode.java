package ru.ksoft.springaiyandexgpt.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.http.HttpStatus;

/**
 * Standard error codes aligned with Google / Yandex API conventions.
 * <p>
 * Includes a success code (OK) and error codes. Each constant has a numeric value and a matching HTTP status.
 */
public enum ErrorCode {
    /**
     * Not an error; returned on success.
     * <p>
     * HTTP Mapping: 200 OK
     */
    OK(0, HttpStatus.OK),

    /**
     * The operation was cancelled, typically by the caller.
     * <p>
     * HTTP Mapping: 423 Locked
     */
    CANCELLED(1, HttpStatus.LOCKED),

    /**
     * Unknown error.
     * <p>
     * HTTP Mapping: 500 Internal Server Error
     */
    UNKNOWN(2, HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * The client specified an invalid argument.
     * <p>
     * HTTP Mapping: 400 Bad Request
     */
    INVALID_ARGUMENT(3, HttpStatus.BAD_REQUEST),

    /**
     * Deadline expired before the operation completed.
     * <p>
     * HTTP Mapping: 504 Gateway Timeout
     */
    DEADLINE_EXCEEDED(4, HttpStatus.GATEWAY_TIMEOUT),

    /**
     * The requested entity was not found.
     * <p>
     * HTTP Mapping: 404 Not Found
     */
    NOT_FOUND(5, HttpStatus.NOT_FOUND),

    /**
     * The entity already exists.
     * <p>
     * HTTP Mapping: 409 Conflict
     */
    ALREADY_EXISTS(6, HttpStatus.CONFLICT),

    /**
     * The caller does not have permission to execute the operation.
     * <p>
     * HTTP Mapping: 403 Forbidden
     */
    PERMISSION_DENIED(7, HttpStatus.FORBIDDEN),

    /**
     * The request does not have valid authentication credentials.
     * <p>
     * HTTP Mapping: 401 Unauthorized
     */
    UNAUTHENTICATED(16, HttpStatus.UNAUTHORIZED),

    /**
     * Resource exhausted (e.g. quota or rate limit).
     * <p>
     * HTTP Mapping: 429 Too Many Requests
     */
    RESOURCE_EXHAUSTED(8, HttpStatus.TOO_MANY_REQUESTS),

    /**
     * Operation rejected because the system is in an invalid state.
     * <p>
     * HTTP Mapping: 400 Bad Request
     */
    FAILED_PRECONDITION(9, HttpStatus.BAD_REQUEST),

    /**
     * Operation aborted due to a concurrency conflict.
     * <p>
     * HTTP Mapping: 409 Conflict
     */
    ABORTED(10, HttpStatus.CONFLICT),

    /**
     * Out of range (e.g. invalid parameter value).
     * <p>
     * HTTP Mapping: 400 Bad Request
     */
    OUT_OF_RANGE(11, HttpStatus.BAD_REQUEST),

    /**
     * Operation is not implemented or not supported.
     * <p>
     * HTTP Mapping: 501 Not Implemented
     */
    UNIMPLEMENTED(12, HttpStatus.NOT_IMPLEMENTED),

    /**
     * Internal server error.
     * <p>
     * HTTP Mapping: 500 Internal Server Error
     */
    INTERNAL(13, HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * The service is currently unavailable.
     * <p>
     * HTTP Mapping: 503 Service Unavailable
     */
    UNAVAILABLE(14, HttpStatus.SERVICE_UNAVAILABLE),

    /**
     * Unrecoverable data loss or corruption.
     * <p>
     * HTTP Mapping: 500 Internal Server Error
     */
    DATA_LOSS(15, HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final HttpStatus httpStatus;

    ErrorCode(int code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /**
     * Numeric code value (as in protobuf-style APIs).
     */
    public int getCode() {
        return code;
    }

    /**
     * Spring {@link HttpStatus} corresponding to this code.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Resolve {@link ErrorCode} by numeric value.
     *
     * @param value numeric code
     * @return matching ErrorCode, or {@code null} if unknown
     */
    public static ErrorCode fromValue(int value) {
        for (ErrorCode ec : values()) {
            if (ec.code == value) {
                return ec;
            }
        }
        return null;
    }

    /**
     * Resolve {@link ErrorCode} by numeric value, throwing if unknown.
     *
     * @param value numeric code
     * @return matching ErrorCode
     * @throws IllegalArgumentException if the code is unknown
     */
    @JsonCreator
    public static ErrorCode fromValueOrThrow(int value) {
        ErrorCode ec = fromValue(value);
        if (ec == null) {
            throw new IllegalArgumentException("Unknown error code: " + value);
        }
        return ec;
    }
}
