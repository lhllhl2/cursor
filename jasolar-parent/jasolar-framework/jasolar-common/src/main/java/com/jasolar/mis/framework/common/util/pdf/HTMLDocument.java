package com.jasolar.mis.framework.common.util.pdf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import com.itextpdf.text.FontProvider;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.html.ImageProvider;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * HTML文档
 * 
 * @author galuo
 * @date 2022/04/09
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class HTMLDocument extends AbstractHTML {

    /** 页面大小 */
    @Builder.Default
    protected Rectangle pageSize = PageSize.A4;

    /** 字符集编码,默认为UTF-8 */
    @Builder.Default
    protected Charset charset = StandardCharsets.UTF_8;

    /** 使用的CSS样式 */
    @Builder.Default
    protected CSSResolver cssResolver = PdfUtils.DEFAULT_CSS_RESOLVER;

    /** 提供字体 */
    @Builder.Default
    protected FontProvider fontProvider = PdfUtils.DEFAULT_FONT_PROVIDER;

    /** 解析图片,默认图片所在目录为images */
    @Builder.Default
    protected ImageProvider imageProvider = PdfUtils.DEFAULT_IMAGE_PROVIDER;

    /** 页眉 */
    @Nullable
    protected HTMLHeader header;

    /** 页脚 */
    @Nullable
    protected HTMLFooter footer;

    /** PdfPageEvent, 可以自定义分页的页眉页脚 */
    @Nullable
    protected PdfPageEvent pageEvent;

}
