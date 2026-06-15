package ru.kechlab.springaiyandexgpt.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

/**
 * Chat message roles accepted by the Yandex completion API ({@code user}, {@code assistant},
 * {@code system}).
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    @JsonValue
    private final String value;

    @JsonCreator
    public static Role fromValue(String value) {
        Assert.notNull(value, "Value cannot be null");
        for (Role role : values()) {
            if (role.getValue().equalsIgnoreCase(value.strip())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }
}
