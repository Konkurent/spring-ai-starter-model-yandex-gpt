package ru.ksoft.springaiyandexgpt.constants;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import ru.ksoft.springaiyandexgpt.image.YandexAiImageOptions;

/**
 * Supported output image MIME types for Yandex image generation (JPEG and PNG).
 */
@Getter
@RequiredArgsConstructor
public enum YandexMimeTypeEnum implements YandexAiImageOptions.YandexMimeType {
    IMAGE_JPEG(MimeTypeUtils.IMAGE_JPEG),
    IMAGE_PNG(MimeTypeUtils.IMAGE_PNG);

    private final MimeType mimeType;

    @Override
    public String getType() {
        return mimeType.getType();
    }
}
