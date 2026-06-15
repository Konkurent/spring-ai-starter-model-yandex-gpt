package ru.kechlab.springaiyandexgpt.constants;

/**
 * Status of a single completion alternative in streaming and non-streaming responses (partial, final,
 * filtered, tool calls, etc.).
 */
public enum CompletionResponseStatus {
    ALTERNATIVE_STATUS_UNSPECIFIED,
    ALTERNATIVE_STATUS_PARTIAL,
    ALTERNATIVE_STATUS_TRUNCATED_FINAL,
    ALTERNATIVE_STATUS_FINAL,
    ALTERNATIVE_STATUS_CONTENT_FILTER,
    ALTERNATIVE_STATUS_TOOL_CALLS;
}
