# Spring AI — Yandex Foundation Models

**Language / Язык:** [English](#english) · [Русский](#russian)

Integration of [Yandex Cloud Foundation Models](https://yandex.cloud/en/docs/foundation-models/) (YandexGPT and related APIs) with [Spring AI](https://spring.io/projects/spring-ai): portable `ChatModel`, `EmbeddingModel`, and `ImageModel` built on the same ideas as the official [OpenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html) documentation — prerequisites, dependencies, connectivity, per-feature configuration, retries, and observability.

---

## English

### Overview

This repository is a multi-module build:

| Module | Role |
|--------|------|
| `spring-ai-yandex-gpt` | Core client and Spring AI model implementations (`YandexAiChatModel`, `YandexAiEmbeddingModel`, `YandexAiImageModel`, HTTP APIs, auth processors). |
| `spring-ai-yandex-gpt-autoconfigure` | Spring Boot auto-configuration: beans, `spring.ai.*` properties, observation hooks. |
| `spring-ai-starter-model-yandex-gpt` | Aggregating starter: one dependency that pulls auto-configuration (and transitively the core module). |

Aligns with Spring AI 1.1.x-style configuration: you select the provider with `spring.ai.model.chat`, `spring.ai.model.embedding`, `spring.ai.model.image`, and `spring.ai.auth`, then tune Yandex-specific namespaces (`spring.ai.model.*.yandexai`, `spring.ai.auth.yandexai`).

### Requirements

- **Java** 17+
- **Spring Boot** 3.5.13 (see `spring-ai-yandex-gpt-parent` / root `pom.xml`)
- **Spring AI** 1.1.x via `spring-ai-bom`
- A Yandex Cloud **folder ID** and **credentials** (IAM token and/or service account API key) as required by the [Foundation Models API](https://yandex.cloud/en/docs/foundation-models/)

### Dependencies

Import the [Spring AI BOM](https://docs.spring.io/spring-ai/reference/getting-started.html#dependency-management) for aligned versions, then add the Yandex starter (publish to your registry or `mvn install` locally from the `spring-ai-yandex-gpt-parent` reactor).

**Maven**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>ru.ksoft</groupId>
        <artifactId>spring-ai-starter-model-yandex-gpt</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("ru.ksoft:spring-ai-starter-model-yandex-gpt:1.0.1-SNAPSHOT")
}
```

Replace the version with your release. The starter depends only on `spring-ai-yandex-gpt-autoconfigure`, which in turn depends on `spring-ai-yandex-gpt`. You can depend on the two modules separately if you do not want the starter artifact. The core module brings `spring-ai-model`, `spring-ai-retry`, WebFlux (streaming), and Jackson; see `spring-ai-yandex-gpt/pom.xml` for the full graph.

### Prerequisites (Yandex Cloud)

- A [cloud and folder](https://yandex.cloud/en/docs/resource-manager/concepts/resources-hierarchy#folder) in Yandex Cloud.
- **Folder ID** — used in model URIs (`gpt://{folderId}/…`) and in configuration.
- **Authentication** — e.g. [IAM token](https://yandex.cloud/en/docs/iam/concepts/authorization/iam-token) or a service account API key, per Yandex documentation.

Do not commit secrets. Use environment variables or a secret store.

Properties (see `AuthProperties` / `additional-spring-configuration-metadata.json`):

- `spring.ai.auth.yandexai.api-key`
- `spring.ai.auth.yandexai.iam.token`
- `spring.ai.auth.yandexai.iam.token-file`
- `spring.ai.auth.yandexai.iam.refresh-interval` (default 1 hour when using a file)

**YAML example** (`iam` is a nested map — not `iam.token` as a single key):

```yaml
spring:
  ai:
    model:
      folder-id: ${YANDEX_FOLDER_ID}
    auth:
      yandexai:
        api-key: ${YANDEX_API_KEY}
        # or IAM instead of / in addition to api-key:
        # iam:
        #   token: ${YANDEX_IAM_TOKEN}
        #   token-file: ${YANDEX_IAM_TOKEN_FILE}
        #   refresh-interval: 1h
```

### Connectivity

| Concept | Description |
|--------|-------------|
| `spring.ai.model.base-url` | HTTP base for the LLM API. Default in code: `https://llm.api.cloud.yandex.net`. |
| Chat path | `spring.ai.model.chat.yandexai.completion-path` (default `/foundationModels/v1/completions`). |
| Embedding path | `spring.ai.model.embedding.yandexai.embedding-path` (default `/foundationModels/v1/embeddings`). |
| Image path | `spring.ai.model.image.yandexai.image-path` (default `/foundationModels/v1/imageGenerationAsync`). |
| Operations | `spring.ai.model.operation-path` — path segment for `OperationClient` (default `operations` in `YandexAiProperties`; combined with the operation id when polling). |

Non-streaming calls use `RestClient`; streaming chat uses `WebClient`.

The `spring-ai-yandex-gpt-autoconfigure` module depends on **`spring-boot-starter-webflux`**, so applications that use the starter (or autoconfigure) get auto-configured `RestClient.Builder` and `WebClient.Builder`, JSON codecs, and **Reactor Netty** as the preferred reactive HTTP connector (see [Spring Boot — Calling REST Services](https://docs.spring.io/spring-boot/reference/io/rest-client.html)). If you depend only on `spring-ai-yandex-gpt` without autoconfigure, you must supply compatible HTTP infrastructure yourself.

### Enabling auto-configuration

Set the Spring AI provider keys. Because `spring.ai.model.chat` is both a scalar (`yandexai`) and a prefix for nested keys (`spring.ai.model.chat.yandexai.*`), the examples below use `application.properties` (you can express the same keys in YAML with bracket notation, e.g. `spring.ai.model.chat.yandexai.model`).

```properties
spring.ai.auth=yandexai
spring.ai.model.chat=yandexai
spring.ai.model.embedding=yandexai
spring.ai.model.image=yandexai
spring.ai.model.folder-id=${YANDEX_FOLDER_ID}
# spring.ai.model.embedding.yandexai.metadata-mode=NONE
```

**Embeddings `metadata-mode`:** defaults to **`EMBED`** in `EmbeddingsProperties` (suitable for typical `embed(Document)` usage). Override with `spring.ai.model.embedding.yandexai.metadata-mode` when you need `NONE`, `INFERENCE`, `ALL`, etc.

**Defaults:** `YandexAiAuthAutoConfiguration`, `YandexAiChatAutoConfiguration`, `YandexAiEmbeddingAutoConfiguration`, and `YandexAiImageAutoConfiguration` use `matchIfMissing = true`, so `spring.ai.auth`, `spring.ai.model.chat`, `spring.ai.model.embedding`, and `spring.ai.model.image` default to `yandexai` when unset. You still need a **folder id** and **credentials** (`api-key` and/or `iam.*`).

Registered auto-configuration classes (see `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`): `YandexAiAutoConfiguration`, `YandexAiAuthAutoConfiguration`, `YandexAiChatAutoConfiguration`, `YandexAiImageAutoConfiguration`, `YandexAiEmbeddingAutoConfiguration`.

### Chat model

**Class:** `ru.ksoft.springaiyandexgpt.text.YandexAiChatModel`

- Synchronous `call(Prompt)` and `stream(Prompt)`.
- Observation provider name: `yandex_gpt`.

**Options** (`YandexAiChatOptions`, implements `StructuredOutputChatOptions`): stored **`completionModel`** (`CompletionModel`, URI via `gpt://{folderId}/{modelName}`); Boot property **`spring.ai.model.chat.yandexai.model`**. Also `folderId`, `temperature`, **`maxTokens`** (`Integer`), `reasoningMode`, `stopSequences`, structured output (`outputClass` / `outputSchema`), optional `httpHeaders`. For Spring AI’s `ChatOptions#getModel()`, the implementation returns the model **id string** (`CompletionModel#getName()`).

Generic `ChatOptions` fields such as `frequencyPenalty`, `presencePenalty`, `topP`, and `topK` are **not** mapped to this API (warnings may be logged).

**Messages:** `USER`, `SYSTEM`, and `ASSISTANT` are supported. **Tool calling** is not supported — using tool APIs leads to `UnsupportedOperationException`.

**Models:** see enum `CompletionModel` (e.g. `YANDEX_GPT_PRO_5_1`, `YANDEX_GPT_PRO_5`, `YANDEX_GPT_LITE_5`, `ALICE_AI`) and the Yandex Cloud console for the authoritative list.

**Headers:** `UserAgentHeadersProcessor` sets `User-Agent: spring-ai`, similar in spirit to Spring AI’s OpenAI client.

### Embedding model

**Class:** `ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingModel`

- Extends `AbstractEmbeddingModel` and calls the embedding HTTP API.

**Options:** `folderId`, `dimensions` (default `256`, sent as `dim` in the request body).

**Model URIs:** enum `EmbeddingModel` (`DOC`, `QUERY`, `TUNING` templates).

**Metadata:** `spring.ai.model.embedding.yandexai.metadata-mode` defaults to **`EMBED`**; set explicitly only if another `MetadataMode` fits your pipeline.

### Image model

**Class:** `ru.ksoft.springaiyandexgpt.image.YandexAiImageModel`

Asynchronous generation with operation polling (`OperationService`, `OperationClient`). Configure MIME type, seed, and aspect ratio under `spring.ai.model.image.yandexai.options.*`.

### Retries

Shared HTTP client settings use `spring.ai.model.client.timeout` and nested **`spring.ai.model.client.retry.*`** (see `ClientProperties.RetryProperties`):

| Property | Role |
|----------|------|
| `spring.ai.model.client.retry.max-attempts` | Maximum attempts (default `1` in `ClientProperties`). |
| `spring.ai.model.client.retry.multiplier` | Backoff multiplier. |
| `spring.ai.model.client.retry.min-backoff` | Initial backoff (**milliseconds**). |
| `spring.ai.model.client.retry.max-backoff` | Max backoff (**milliseconds**). |

Override per feature with the same leaf names under:

- `spring.ai.model.chat.yandexai.client.*`
- `spring.ai.model.embedding.yandexai.client.*`
- `spring.ai.model.image.yandexai.client.*`

Async image **operation polling** has a separate nested client: **`spring.ai.model.image.yandexai.operation.client.*`** (timeout and the same retry keys).

These map to `ClientSpec.RetrySpec` / `RetryExecutor` in this project.

Spring AI’s global retry namespace (`spring.ai.retry.*`) applies when you use Spring AI’s shared retry support — same idea as in the [OpenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html) reference.

### Observability

Chat uses `ChatModelObservationDocumentation`; embeddings use `EmbeddingModelObservationDocumentation`; images use the image observation autoconfigure from Spring AI. Provider context uses the name `yandex_gpt` where applicable.

### Manual bean configuration

If you prefer not to use auto-configuration, declare `YandexAiChatApi`, `YandexAiEmbeddingApi`, options, `ClientSpec.RetrySpec`, and model beans yourself. Paths and host must match the current Yandex API (defaults are defined in `ChatProperties`, `EmbeddingsProperties`, and `ImageProperties`).

### Reference links

- [Spring AI — Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html)
- [Spring AI — OpenAI Chat (doc layout reference)](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [Yandex Cloud — Foundation Models](https://yandex.cloud/en/docs/foundation-models/)

---

## Russian

### Обзор

Этот репозиторий — многомодульный проект:

| Модуль | Назначение |
|--------|------------|
| `spring-ai-yandex-gpt` | Ядро: HTTP-клиенты и реализации `ChatModel`, `EmbeddingModel`, `ImageModel`, обработка заголовков и авторизации. |
| `spring-ai-yandex-gpt-autoconfigure` | Автоконфигурация Spring Boot: бины, свойства `spring.ai.*`, наблюдаемость. |
| `spring-ai-starter-model-yandex-gpt` | Агрегирующий стартер: одна зависимость подтягивает autoconfigure и транзитивно core. |

Конфигурация в духе Spring AI 1.1.x: провайдер задаётся через `spring.ai.model.chat`, `spring.ai.model.embedding`, `spring.ai.model.image` и `spring.ai.auth`, детали — в пространствах имён Yandex (`spring.ai.model.*.yandexai`, `spring.ai.auth.yandexai`).

### Требования

- **Java** 17+
- **Spring Boot** 3.5.13 (версия в корневом `pom.xml` модуля `spring-ai-yandex-gpt-parent`)
- **Spring AI** 1.1.x через `spring-ai-bom`
- **Каталог Yandex Cloud** и **учётные данные** (IAM-токен и/или API-ключ сервисного аккаунта) согласно [документации Foundation Models](https://yandex.cloud/ru/docs/foundation-models/)

### Зависимости

Подключите [Spring AI BOM](https://docs.spring.io/spring-ai/reference/getting-started.html#dependency-management), затем артефакт `spring-ai-starter-model-yandex-gpt` (после публикации в ваш реестр или `mvn install` из реактора `spring-ai-yandex-gpt-parent`).

**Maven** — см. блок выше в английской секции; замените `1.0.1-SNAPSHOT` на вашу версию.

**Gradle** — аналогично английской секции.

Стартер зависит только от `spring-ai-yandex-gpt-autoconfigure`, тот — от `spring-ai-yandex-gpt`; при необходимости можно подключать два модуля без артефакта стартера.

### Предварительные условия (Yandex Cloud)

- Облако и [каталог](https://yandex.cloud/ru/docs/resource-manager/concepts/resources-hierarchy#folder).
- **ID каталога** — для URI моделей и свойств Spring.
- **Авторизация** — например [IAM-токен](https://yandex.cloud/ru/docs/iam/concepts/authorization/iam-token) или API-ключ сервисного аккаунта.

Секреты в репозиторий не кладём; используйте переменные окружения или хранилище секретов. Ключи: `spring.ai.auth.yandexai.api-key`, `spring.ai.auth.yandexai.iam.token`, `spring.ai.auth.yandexai.iam.token-file`, `spring.ai.auth.yandexai.iam.refresh-interval`. Пример YAML — в английской секции выше (`iam` — вложенная карта).

### Подключение к API

| Параметр | Описание |
|----------|----------|
| `spring.ai.model.base-url` | Базовый URL API. По умолчанию: `https://llm.api.cloud.yandex.net`. |
| Путь чата | `spring.ai.model.chat.yandexai.completion-path` (по умолчанию `/foundationModels/v1/completions`). |
| Путь эмбеддингов | `spring.ai.model.embedding.yandexai.embedding-path` (по умолчанию `/foundationModels/v1/embeddings`). |
| Путь изображений | `spring.ai.model.image.yandexai.image-path` (по умолчанию `/foundationModels/v1/imageGenerationAsync`). |
| Операции | `spring.ai.model.operation-path` — сегмент пути для `OperationClient` (по умолчанию `operations` в `YandexAiProperties`; к нему добавляется id операции). |

Синхронные вызовы — через `RestClient`, стриминг чата — через `WebClient`.

Модуль `spring-ai-yandex-gpt-autoconfigure` зависит от **`spring-boot-starter-webflux`**: при подключении стартера или autoconfigure в контексте появляются настроенные `RestClient.Builder` и `WebClient.Builder`, кодеки JSON и **Reactor Netty** как предпочтительный реактивный HTTP-клиент. Если используется только `spring-ai-yandex-gpt` без autoconfigure, HTTP-инфраструктуру нужно обеспечить самостоятельно.

### Включение автоконфигурации

Задайте провайдеры Spring AI. У ключей вида `spring.ai.model.chat` и `spring.ai.model.chat.yandexai.*` одна и та же «ветка» в плоском виде удобнее выражается через `application.properties` (аналогичные ключи в YAML задают через квадратные скобки).

```properties
spring.ai.auth=yandexai
spring.ai.model.chat=yandexai
spring.ai.model.embedding=yandexai
spring.ai.model.image=yandexai
spring.ai.model.folder-id=${YANDEX_FOLDER_ID}
# spring.ai.model.embedding.yandexai.metadata-mode=NONE
```

**Режим метаданных эмбеддингов:** по умолчанию **`EMBED`** в `EmbeddingsProperties`. Переопределяйте `spring.ai.model.embedding.yandexai.metadata-mode`, если нужны `NONE`, `INFERENCE`, `ALL` и т.д.

**По умолчанию:** у автоконфигураций auth, chat, embedding и image задано `matchIfMissing = true`, поэтому при отсутствии свойств `spring.ai.auth`, `spring.ai.model.chat`, `spring.ai.model.embedding` и `spring.ai.model.image` подставляется `yandexai`. Всё равно нужны **folder id** и **учётные данные** (`api-key` и/или `iam.*`).

Классы авто-конфигурации перечислены в `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`: `YandexAiAutoConfiguration`, `YandexAiAuthAutoConfiguration`, `YandexAiChatAutoConfiguration`, `YandexAiImageAutoConfiguration`, `YandexAiEmbeddingAutoConfiguration`.

### Чат-модель

**Класс:** `ru.ksoft.springaiyandexgpt.text.YandexAiChatModel`

- Синхронный `call(Prompt)` и `stream(Prompt)`.
- Имя провайдера в наблюдаемости: `yandex_gpt`.

**Опции** (`YandexAiChatOptions`, `StructuredOutputChatOptions`): поле **`completionModel`** (`CompletionModel`, URI `gpt://{folderId}/{modelName}`); в конфигурации — **`spring.ai.model.chat.yandexai.model`**. Также `folderId`, `temperature`, **`maxTokens`** (`Integer`), `reasoningMode`, стоп-последовательности, структурированный вывод (`outputClass` / `outputSchema`), заголовки. `ChatOptions#getModel()` отдаёт **строковый id** модели (`CompletionModel#getName()`).

Поля общего `ChatOptions` вроде `frequencyPenalty`, `presencePenalty`, `topP`, `topK` **не** пробрасываются в API Yandex.

**Сообщения:** поддерживаются `USER`, `SYSTEM`, `ASSISTANT`. **Вызов инструментов** не поддерживается.

**Модели:** перечисление `CompletionModel`; актуальный список — в консоли Yandex Cloud.

### Модель эмбеддингов

**Класс:** `ru.ksoft.springaiyandexgpt.embeddings.YandexAiEmbeddingModel`

**Параметры:** `folderId`, размерность `dimensions` (по умолчанию `256`).

**URI моделей:** enum `EmbeddingModel`.

**Метаданные:** `spring.ai.model.embedding.yandexai.metadata-mode` по умолчанию **`EMBED`**; задавайте явно только если нужен другой `MetadataMode`.

### Модель изображений

**Класс:** `ru.ksoft.springaiyandexgpt.image.YandexAiImageModel`

Асинхронная генерация с опросом операции. Настройки MIME, seed и соотношения сторон — под `spring.ai.model.image.yandexai.options.*`.

### Повторы запросов

Общие настройки: `spring.ai.model.client.timeout` и **`spring.ai.model.client.retry.*`** — `max-attempts`, `multiplier`, `min-backoff`, `max-backoff` (задержки в **миллисекундах**).

Переопределение для конкретной функции — те же имена листьев под:

- `spring.ai.model.chat.yandexai.client.*`
- `spring.ai.model.embedding.yandexai.client.*`
- `spring.ai.model.image.yandexai.client.*`

Для опроса долгих операций генерации изображений — отдельно **`spring.ai.model.image.yandexai.operation.client.*`**.

Глобальные настройки Spring AI `spring.ai.retry.*` — по той же идее, что в официальной документации для OpenAI.

### Наблюдаемость

Используются стандартные документации наблюдения Spring AI для чата, эмбеддингов и изображений; провайдер — `yandex_gpt` там, где это задано в коде.

### Ручная конфигурация бинов

Можно не использовать автоконфигурацию и собрать `YandexAiChatApi`, `YandexAiEmbeddingApi`, опции и модели вручную. Пути и хост должны соответствовать актуальной документации Yandex.

### Ссылки

- [Spring AI — Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html)
- [Spring AI — OpenAI Chat (ориентир по структуре документа)](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [Yandex Cloud — Foundation Models (RU)](https://yandex.cloud/ru/docs/foundation-models/)
