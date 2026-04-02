# YandexGPT — интеграция с Spring AI

Модуль предоставляет реализации `ChatModel` и `EmbeddingModel` для [Foundation Models в Yandex Cloud](https://yandex.cloud/ru/docs/foundation-models/) (YandexGPT и связанные модели), совместимые с абстракциями Spring AI (`Prompt`, `ChatResponse`, `EmbeddingRequest` и т.д.).

Структура этого документа сознательно повторяет [официальную документацию Spring AI для OpenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html): предварительные требования, зависимости, настройка подключения, чат, эмбеддинги и повторные попытки.

---

## Предварительные требования

Для вызова API Yandex Cloud нужны:

- Каталог и [облако](https://yandex.cloud/ru/docs/overview/) в Yandex Cloud.
- Идентификатор [каталога](https://yandex.cloud/ru/docs/resource-manager/concepts/resources-hierarchy#folder) (`folder-id`) — он используется в URI модели и в опциях клиента.
- Способ аутентификации к API Foundation Models (например, [IAM-токен](https://yandex.cloud/ru/docs/iam/concepts/authorization/iam-token) или API-ключ сервисного аккаунта в заголовке `Authorization`), в соответствии с [документацией Yandex Cloud](https://yandex.cloud/ru/docs/foundation-models/).

Рекомендуется не хранить секреты в репозитории: задавайте их через переменные окружения или секрет-хранилище и подставляйте в Spring через `application.yml`:

```yaml
yandex:
  cloud:
    folder-id: ${YANDEX_FOLDER_ID}
    iam-token: ${YANDEX_IAM_TOKEN}
```

Имена свойств выше — **пример соглашения**; в коде ниже значения читаются и передаются в ваши бины.

---

## Репозитории и BOM

Артефакты **Spring AI** публикуются в Maven Central. Для согласованных версий Spring AI используйте [Spring AI BOM](https://docs.spring.io/spring-ai/reference/getting-started.html#dependency-management) в родительском POM или в секции `dependencyManagement`, как в официальном руководстве.

Модуль `spring-ai-yandex-gpt` из этого репозитория подключается отдельно (после публикации в ваш реестр или через `install` в локальный Maven).

---

## Подключение зависимости

**Maven**

```xml
<dependency>
    <groupId>ru.ksoft</groupId>
    <artifactId>spring-ai-yandex-gpt</artifactId>
    <version>${revision}</version>
</dependency>
```

Подставьте актуальную версию; при мультимодульной сборке родительский артефакт — `spring-ai-starter-model-yandex-gpt`.

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("ru.ksoft:spring-ai-yandex-gpt:<version>")
}
```

Транзитивно потребуются `spring-ai-model`, `spring-ai-retry`, Spring WebFlux (стриминг чата), Jackson и др. — см. `spring-ai-yandex-gpt/pom.xml`.

---

## Автоконфигурация Spring Boot

В текущей версии репозитория **отдельного стартера с `@AutoConfiguration` нет**: бины `YandexChatApi`, `YandexChatModel`, `EmbeddingApi`, `YandexAiEmbeddingModel` и `RetryTemplate` нужно объявить в `@Configuration` приложения (аналогично тому, как в справочнике Spring AI описывают программную настройку и переопределение клиентов).

Ниже — типовой каркас; точные `baseUrl` и пути методов возьмите из [справочника API Foundation Models](https://yandex.cloud/ru/docs/foundation-models/) для вашей версии API.

---

## Подключение к API (базовый URL и пути)

| Параметр | Назначение |
|----------|------------|
| Базовый URL | Хост API LLM Yandex Cloud (например, `https://llm.api.cloud.yandex.net`) — уточняйте в документации. |
| Путь completion | Путь к методу синхронного/потокового completion (передаётся в `YandexChatApi`). |
| Путь embedding | Путь к методу получения векторного представления текста (передаётся в `EmbeddingApi`). |

Класс `YandexChatApi` склеивает `baseUrl` и `completionPath` и выполняет `POST` на полученный URI. Для стриминга используется `WebClient`; для обычного ответа — `RestClient`.

---

## Повторные попытки (Retry)

Как и в [Spring AI для OpenAI](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html), поведение повторов можно выровнять с префиксом `spring.ai.retry` при использовании стандартного `RetryTemplate` из экосистемы Spring AI (зависимость `spring-ai-retry`).

| Свойство | Описание |
|----------|----------|
| `spring.ai.retry.max-attempts` | Максимум попыток. |
| `spring.ai.retry.backoff.initial-interval` | Начальная пауза backoff. |
| `spring.ai.retry.backoff.multiplier` | Множитель интервала. |
| `spring.ai.retry.backoff.max-interval` | Верхняя граница паузы. |
| `spring.ai.retry.on-client-errors` | Повторять ли при ошибках 4xx. |
| `spring.ai.retry.exclude-on-http-codes` | Коды HTTP без повтора. |
| `spring.ai.retry.on-http-codes` | Коды HTTP, при которых выполнять повтор. |

Вызовы в `YandexChatModel` и `YandexAiEmbeddingModel` оборачиваются в переданный вами `RetryTemplate` через `RetryExecutor`.

---

## Chat Model

### Зависимости от абстракций Spring AI

Реализация `ru.ksoft.springaiyandexgpt.text.YandexChatModel` поддерживает:

- Синхронный вызов `call(Prompt)`.
- Поток `stream(Prompt)` (SSE/поток ответов API через `WebClient`).
- Метаданные ответа и интеграцию с Micrometer Observation (`provider`: `yandex_gpt`).

### Опции чата: `YandexGptChatOptions`

Основные поля (см. класс в пакете `ru.ksoft.springaiyandexgpt.text`):

| Параметр | Описание |
|----------|----------|
| `model` | Модель из перечисления `CompletionModel` (URI собирается как `gpt://{folderId}/{modelName}`). |
| `folderId` | Идентификатор каталога в облаке. |
| `temperature` | Температура сэмплирования (по умолчанию `0.7`). |
| `maxTokens` | Ограничение длины ответа. |
| `reasoningMode` | Режим рассуждений (`ReasoningMode`: например `DISABLED`, `ENABLED_HIDDEN`). |
| `stopSequences` | Стоп-последовательности. |
| `outputClass` / `outputSchema` | Структурированный вывод (JSON Schema генерируется для `outputClass`). |
| `httpHeaders` | Дополнительные заголовки на запрос. |

Параметры вроде `frequencyPenalty`, `presencePenalty`, `topP`, `topK` из общего `ChatOptions` **не поддерживаются** API YandexGPT в данной реализации (в лог пишутся предупреждения).

### Модели completion: `CompletionModel`

Предопределённые значения включают, среди прочего:

- `YANDEX_GPT_PRO_5_1`, `YANDEX_GPT_PRO_5`, `YANDEX_GPT_LITE_5`, `ALICE_AI`.

Актуальный список и идентификаторы смотрите в коде `CompletionModel` и в консоли Yandex Cloud.

### Сообщения

Поддерживаются типы сообщений Spring AI: `USER`, `SYSTEM`, `ASSISTANT`. Вызов инструментов (`ToolCallingChatOptions`) в `YandexChatModel` **не поддерживается** — при его использовании выбрасывается `UnsupportedOperationException`.

### User-Agent

Компонент `UserAgentHeaderProcessor` (если подключён сканированием компонентов) выставляет заголовок `User-Agent: spring-ai` по аналогии с официальным поведением Spring AI для OpenAI.

### Авторизация

Интерфейс `AuthorizationProcessor` / `HeaderProcessor` предназначен для добавления заголовка `Authorization` (и других). Заготовка `ApiKeyAuthorizationProcessor` в репозитории пустая — реализуйте заполнение заголовков под ваш способ входа в Yandex Cloud.

---

## Embedding Model

Класс `ru.ksoft.springaiyandexgpt.embedings.YandexAiEmbeddingModel` расширяет `AbstractEmbeddingModel` и обращается к `EmbeddingApi`.

### Опции: `YandexGptEmbeddingOptions`

| Параметр | Описание |
|----------|----------|
| `folderId` | Каталог для подстановки в URI модели эмбеддинга. |
| `dimensions` | Размерность вектора (по умолчанию `256`), передаётся в теле запроса как `dim`. |

### URI моделей: `EmbeddingModel` (enum)

Шаблоны URI для документов, запросов и кастомной настройки заданы в `ru.ksoft.springaiyandexgpt.constants.EmbeddingModel` (`DOC`, `QUERY`, `TUNING`).

### Запрос к API

`EmbeddingApi` отправляет JSON с полями `modelUri`, `text`, `dim` на указанный путь относительно базового URL.

---

## Пример ручной конфигурации Spring

```java
@Configuration
public class YandexGptClientConfig {

    @Bean
    public YandexChatApi yandexChatApi(
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            ResponseErrorHandler responseErrorHandler,
            List<HeaderProcessor> headerProcessors) {
        return new YandexChatApi(
                "https://llm.api.cloud.yandex.net",
                "/foundationModels/v1/completion",
                headerProcessors,
                restClientBuilder,
                webClientBuilder,
                responseErrorHandler
        );
    }

    @Bean
    public EmbeddingApi embeddingApi(
            RestClient.Builder restClientBuilder,
            List<HeaderProcessor> headerProcessors) {
        return new EmbeddingApi(
                "https://llm.api.cloud.yandex.net",
                "/foundationModels/v1/textEmbedding",
                headerProcessors,
                restClientBuilder
        );
    }

    @Bean
    public YandexGptChatOptions yandexGptChatOptions(@Value("${yandex.cloud.folder-id}") String folderId) {
        return YandexGptChatOptions.builder()
                .folderId(folderId)
                .model(CompletionModel.YANDEX_GPT_PRO_5_1)
                .build();
    }

    @Bean
    public YandexChatModel yandexChatModel(
            YandexChatApi api,
            YandexGptChatOptions options,
            RetryTemplate retryTemplate,
            ObservationRegistry observationRegistry) {
        return new YandexChatModel(api, options, retryTemplate, observationRegistry);
    }

    @Bean
    public YandexAiEmbeddingModel yandexAiEmbeddingModel(
            EmbeddingApi embeddingApi,
            YandexGptEmbeddingOptions embeddingOptions,
            RetryTemplate retryTemplate,
            ObservationRegistry observationRegistry) {
        return new YandexAiEmbeddingModel(
                embeddingApi,
                MetadataMode.NONE,
                embeddingOptions,
                retryTemplate,
                observationRegistry
        );
    }
}
```

Пути `/foundationModels/v1/...` приведены как ориентир — **сверьте с актуальной документацией Yandex Cloud**. Реализацию `HeaderProcessor` для IAM или API-ключа добавьте отдельным бином.

---

## Наблюдаемость

Для чата используется `ChatModelObservationDocumentation`; для эмбеддингов — `EmbeddingModelObservationDocumentation`. Имя провайдера в контексте: `yandex_gpt` (см. `YandexChatModel.PROVIDER_NAME`).

---

## Ссылки

- [Spring AI — Getting Started](https://docs.spring.io/spring-ai/reference/getting-started.html)
- [Spring AI — OpenAI Chat (структура документации)](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
- [Yandex Cloud — Foundation Models](https://yandex.cloud/ru/docs/foundation-models/)
