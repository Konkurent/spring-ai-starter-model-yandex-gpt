package ru.ksoft.springaiyandexgpt.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.ksoft.springaiyandexgpt.autoconfigure.constants.SpringAiYandexGptModelProperties;
import ru.ksoft.springaiyandexgpt.constants.YandexMimeTypeEnum;

/**
 * Image model settings under {@code spring.ai.model.image.yandexai}: async generation path, MIME type,
 * seed, aspect ratio, folder override, and nested client settings for HTTP and operations.
 */
@Data
@ConfigurationProperties(prefix = ImageProperties.PREFIX)
public class ImageProperties {

    public static final String PREFIX = SpringAiYandexGptModelProperties.YANDEX_AI_IMAGE_MODEL;

    /** Default REST path for asynchronous image generation. */
    public static final String IMAGE_PATH = "/foundationModels/v1/imageGenerationAsync";

    public String imagePath = IMAGE_PATH;

    private String folderId;

    private Options options = new Options();

    @NestedConfigurationProperty
    private ClientProperties client;

    @NestedConfigurationProperty
    private OperationProperties operation;

    @Data
    public static class Options {

        private YandexMimeTypeEnum mimeType;

        private Integer seed;

        @NestedConfigurationProperty
        private AspectRatio aspectRatio = new AspectRatio();

        @Data
        public static class AspectRatio {
            private Integer width;
            private Integer height;
        }
    }

    @Data
    public static class OperationProperties {

        private ClientProperties client;

    }
}
