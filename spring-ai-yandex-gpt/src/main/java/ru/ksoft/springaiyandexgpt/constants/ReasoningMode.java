package ru.ksoft.springaiyandexgpt.constants;

public enum ReasoningMode {
    /**
     * Unspecified reasoning mode
     */
    REASONING_MODE_UNSPECIFIED,
    /**
     * Disables reasoning. The model will generate a response without performing any internal reasoning.
     */
    DISABLED,
    /**
     * Enables reasoning in a hidden manner without exposing the reasoning steps to the user
     */
    ENABLED_HIDDEN,
}
