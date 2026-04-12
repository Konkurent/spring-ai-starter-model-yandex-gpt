package ru.ksoft.springaiyandexgpt.autoconfigure.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Picks the most specific non-blank Yandex Cloud folder id from a list (for example common property
 * first, feature-specific property second).
 */
@UtilityClass
public class FolderIdReducer {

    /**
     * @param folderIds ordered candidates; later non-empty values win
     * @return chosen folder id, or {@code null} if none were provided
     */
    @Nullable
    public String reduce(String... folderIds) {
        if (folderIds.length < 2) {
            return folderIds.length == 1 ? folderIds[0] : null;
        }
        return Arrays.stream(folderIds).reduce((left, right) -> right != null && !right.isEmpty() ? right : left).orElse(null);
    }

}
