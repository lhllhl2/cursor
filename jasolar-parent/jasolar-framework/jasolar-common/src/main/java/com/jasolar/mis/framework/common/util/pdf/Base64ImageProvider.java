package com.jasolar.mis.framework.common.util.pdf;

import com.itextpdf.text.Image;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.ImageProvider;
import lombok.SneakyThrows;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

public class Base64ImageProvider extends AbstractImageProvider {
    private final Map<String, String> base64Images = new ConcurrentHashMap<>();
    private final ImageProvider fallbackProvider;

    public Base64ImageProvider(ImageProvider fallbackProvider) {
        this.fallbackProvider = fallbackProvider;
    }

    public void addBase64Image(String src, String base64Data) {
        base64Images.put(src, base64Data);
    }

    @Override
    @SneakyThrows
    public Image retrieve(String src) {
        // 首先检查缓存
        Image img = super.retrieve(src);
        if (img != null) {
            return img;
        }

        // 检查是否有对应的base64数据
        String base64Data = base64Images.get(src);
        if (base64Data != null) {
            // 移除base64前缀（如果存在）
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            img = Image.getInstance(imageBytes);
            store(src, img);
            return img;
        }

        // 如果没有base64数据，使用默认的图片提供者
        return fallbackProvider != null ? fallbackProvider.retrieve(src) : null;
    }

    @Override
    public String getImageRootPath() {
        return fallbackProvider != null ? fallbackProvider.getImageRootPath() : "";
    }
}