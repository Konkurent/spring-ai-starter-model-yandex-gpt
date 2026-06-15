package ru.kechlab.springaiyandexgpt.constants;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.model.ModelDescription;
import ru.kechlab.springaiyandexgpt.dto.ModelTemplate;

/**
 * Embedding model URI templates for document, query, and tuning flows.
 * <p>
 * Placeholders in each template are filled by {@link #formatUri(String...)} with folder id (and tuning
 * parameters where required).
 */
@RequiredArgsConstructor
public enum EmbeddingModel implements ModelTemplate, ModelDescription {

    DOC(EmbeddingPrefix.EMB + "://%s/text-search-doc/latest"),
    QUERY(EmbeddingPrefix.EMB + "://%s/text-search-query/latest"),
    TUNING(EmbeddingPrefix.GPT + "://%s/text-embeddings/%s@%s");

    private final String template;

    @Override
    public String formatUri(String... args) {
        return template.formatted((Object[]) args);
    }

    @Override
    public @NonNull String getName() {
        return name();
    }
}
