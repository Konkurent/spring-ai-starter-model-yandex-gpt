package ru.ksoft.springaiyandexgpt.constants;

import lombok.RequiredArgsConstructor;
import ru.ksoft.springaiyandexgpt.dto.Model;

/**
 * Embedding model URI templates for document, query, and tuning flows.
 * <p>
 * Placeholders in each template are filled by {@link #getUri(String...)} with folder id (and tuning
 * parameters where required).
 */
@RequiredArgsConstructor
public enum EmbeddingModel implements Model {

    DOC("emb://%s/text-search-doc/latest"),
    QUERY("emb://%s/text-search-query/latest"),
    TUNING("gpt://%s/text-embeddings/%s@%s");

    private final String template;

    @Override
    public String getUri(String... args) {
        return template.formatted((Object[]) args);
    }

    @Override
    public String getName() {
        return name();
    }
}
