package ru.ksoft.springaiyandexgpt.image;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageOptions;
import ru.ksoft.springaiyandexgpt.constants.YandexMimeTypeEnum;

/**
 * Options for Yandex image generation: folder id, model URI, MIME type, random seed, and aspect ratio.
 * <p>
 * {@link #merge(ImageOptions)} accepts generic Spring AI {@link ImageOptions} and maps supported
 * fields; unsupported fields are ignored or logged. Default model URI is derived from the folder when
 * {@link #model} is blank.
 */
@Data
@Slf4j
public class YandexAiImageOptions implements ImageOptions {

    private String folderId;

    private String model;

    private YandexMimeType mimeType;

    private Integer seed;

    private Integer widthRatio;

    private Integer heightRatio;

    public YandexAiImageOptions.Builder mutate() {
        return new Builder(this);
    }

    public YandexAiImageOptions merge(ImageOptions imageOptions) {
        YandexAiImageOptions.Builder merger = mutate();
        if (imageOptions instanceof YandexAiImageOptions yaOptions) {
            if (yaOptions.getHeightRatio() != null) {
                merger.heightRatio(yaOptions.getHeightRatio());
            }
            if (yaOptions.getWidthRatio() != null) {
                merger.widthRatio(imageOptions.getWidth());
            }
            if (yaOptions.getModel() != null) {
                merger.model(yaOptions.getModel());
            }
            if (yaOptions.getFolderId() != null) {
                merger.folderId(yaOptions.getFolderId());
            }
            if (yaOptions.getMimeType() != null) {
                merger.mimeType(yaOptions.getMimeType());
            }
        } else {
            if (imageOptions.getN() != null) {
                log.warn("Parameter 'N' is not supported.");
            }
            if (imageOptions.getHeight() != null) {
                log.debug("The 'height' parameter has been assigned to the 'heightRatio' parameter.");
                merger.heightRatio(imageOptions.getHeight());
            }
            if (imageOptions.getWidth() != null) {
                log.debug("The 'width' parameter has been assigned to the 'widthRatio' parameter.");
                merger.widthRatio(imageOptions.getWidth());
            }
            if (imageOptions.getModel() != null) {
                merger.model(imageOptions.getModel());
            }
            if (imageOptions.getResponseFormat() != null) {
                log.debug("The 'responseFormat' parameter has been assigned to the 'mimeType' parameter.");
                merger.mimeType = imageOptions::getResponseFormat;
            }
        }
        return merger.build();
    }

    YandexAiImageOptions(String folderId, String model, YandexMimeType mimeType, Integer seed, Integer widthRatio, Integer heightRatio) {
        this.folderId = folderId;
        this.model = model;
        this.mimeType = mimeType;
        this.seed = seed;
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Integer getN() {
        return null;
    }

    @Override
    public String getModel() {
        if (model == null || model.isBlank()) {
            return "art://" + folderId + "/yandex-art-2.0";
        } else {
            return model;
        }
    }

    @Override
    public Integer getWidth() {
        return widthRatio;
    }

    @Override
    public Integer getHeight() {
        return heightRatio;
    }

    @Override
    public String getResponseFormat() {
        return mimeType.getType();
    }

    @Override
    public String getStyle() {
        return null;
    }

    public interface YandexMimeType {

        YandexMimeType IMAGE_JPEG = YandexMimeTypeEnum.IMAGE_JPEG;
        YandexMimeType IMAGE_PNG = YandexMimeTypeEnum.IMAGE_PNG;

        @JsonValue
        String getType();

        static YandexMimeType of(String type) {
            return new YandexMimeType() {
                @Override
                public String getType() {
                    return type;
                }
            };
        }
    }

    public static class Builder {
        private String folderId;
        private String model;
        private YandexMimeType mimeType;
        private Integer seed;
        private Integer widthRatio;
        private Integer heightRatio;

        Builder() {}

        Builder(YandexAiImageOptions options) {
            this.folderId = options.getFolderId();
            this.mimeType = options.getMimeType();
            this.seed = options.getSeed();
            this.widthRatio = options.getWidthRatio();
            this.heightRatio = options.getHeightRatio();
        }

        public Builder folderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder mimeType(YandexMimeType mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder seed(Integer seed) {
            this.seed = seed;
            return this;
        }

        public Builder widthRatio(Integer widthRatio) {
            this.widthRatio = widthRatio;
            return this;
        }

        public Builder heightRatio(Integer heightRatio) {
            this.heightRatio = heightRatio;
            return this;
        }

        public YandexAiImageOptions build() {
            return new YandexAiImageOptions(this.folderId, this.model, this.mimeType, this.seed, this.widthRatio, this.heightRatio);
        }

        public String toString() {
            return "YandexAiImageOptions.Builder(folderId=" + this.folderId + ", mimeType=" + this.mimeType + ", seed=" + this.seed + ", widthRatio=" + this.widthRatio + ", heightRatio=" + this.heightRatio + ")";
        }
    }
}
