package com.jasolar.mis.framework.common.util.pdf;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StreamUtils;

import com.itextpdf.text.Image;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;

import lombok.SneakyThrows;

/**
 * 可以读取JAR中的图片文件
 * 
 * @author galuo
 * @date 2022/04/09
 */
public class ImageResourceProvider extends AbstractImageProvider {

    /** 所有缓存对象 */
    static final Map<String, ImageResourceProvider> CACHE = new ConcurrentHashMap<>(4);

    /**
     * 指定根目录
     * 
     * @param imageRootPath
     * @return
     */
    public static ImageResourceProvider of(String imageRootPath) {
        ImageResourceProvider provider = CACHE.get(imageRootPath);
        if (provider == null) {
            provider = new ImageResourceProvider(imageRootPath);
            CACHE.put(imageRootPath, provider);
        }
        return provider;
    }

    /** 图片根目录 */
    private String imageRootPath;
    /** 路径分隔符 */
    static final String SEPARATOR = "/";

    /**
     * 指定图片根目录
     * 
     * @param imageRootPath
     */
    private ImageResourceProvider(String imageRootPath) {
        super();
        this.imageRootPath = imageRootPath.endsWith(SEPARATOR) ? imageRootPath.substring(0, imageRootPath.length() - 1) : imageRootPath;
    }

    @Override
    @SneakyThrows
    public Image retrieve(String src) {
        Image img = super.retrieve(src);
        if (img != null) {
            return img;
        }

        try (InputStream in = getClass().getResourceAsStream(imageRootPath + (src.startsWith(SEPARATOR) ? "" : SEPARATOR) + src);) {
            byte[] bytes = StreamUtils.copyToByteArray(in);
            img = com.itextpdf.text.Image.getInstance(bytes);
            this.store(src, img);
        }
        return img;
    }

    @Override
    public String getImageRootPath() {
        return imageRootPath;
    }
}
