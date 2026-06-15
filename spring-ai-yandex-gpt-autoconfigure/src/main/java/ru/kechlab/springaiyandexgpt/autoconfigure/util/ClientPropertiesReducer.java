package ru.kechlab.springaiyandexgpt.autoconfigure.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import ru.kechlab.springaiyandexgpt.autoconfigure.properties.ClientProperties;

import java.util.Arrays;

/**
 * Merges several {@link ClientProperties} instances in order: later values override timeouts and retry
 * fields when present.
 */
@UtilityClass
public class ClientPropertiesReducer {

    /**
     * @param properties ordered from general to specific; at least one non-null entry is expected in practice
     * @return merged properties, or {@code null} if no inputs were passed
     */
    @Nullable
    public ClientProperties reduce(ClientProperties... properties) {
        if (properties.length < 2) {
            return properties.length == 1 ? properties[0] : null;
        }
        return Arrays.stream(properties).reduce(
                (left, right) -> {
                    if (right.getTimeout() != null) {
                        left.setTimeout(right.getTimeout());
                    }
                    if (left.getRetry() == null) {
                        left.setRetry(right.getRetry());
                    } else {
                        if (right.getRetry() != null ) {
                            ClientProperties.RetryProperties rightRetry = right.getRetry();
                            if (rightRetry.getMaxAttempts() != null) {
                                left.getRetry().setMaxAttempts(rightRetry.getMaxAttempts());
                            }
                            if (rightRetry.getMultiplier() != null) {
                                left.getRetry().setMultiplier(rightRetry.getMultiplier());
                            }
                            if (rightRetry.getMinBackoff() != null) {
                                left.getRetry().setMinBackoff(rightRetry.getMinBackoff());
                            }
                            if (rightRetry.getMaxBackoff() != null) {
                                left.getRetry().setMaxBackoff(rightRetry.getMaxBackoff());
                            }
                        }
                    }
                    return left;
                }
        ).orElse(null);
    }

}
