package ru.ksoft.springaiyandexgpt.constants;

/**
 * Tool-calling policy values in chat completion requests (when tools are present in the API payload).
 */
public enum ToolChoiceMode {
    TOOL_CHOICE_MODE_UNSPECIFIED,
    NONE,
    AUTO,
    REQUIRED
}
