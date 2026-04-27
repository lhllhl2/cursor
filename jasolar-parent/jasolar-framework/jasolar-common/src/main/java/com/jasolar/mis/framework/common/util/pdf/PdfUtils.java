package com.jasolar.mis.framework.common.util.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.pipeline.html.ImageProvider;

import lombok.SneakyThrows;

/**
 * PDF工具类.
 * 
 * @author LuoGang
 * @date 2017-04-11 15:15
 */
public class PdfUtils {

    /** 默认的CSS Resolver, 使用的是resources根目录下的/default.css */
    public static final CSSResolver DEFAULT_CSS_RESOLVER = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
    /** 默认的Font Provider, 使用系统的字体. 如果有自定义字体,注意可以通过dockerfile添加到/usr/share/fonts目录 */
    public static final XMLWorkerFontProvider DEFAULT_FONT_PROVIDER = new XMLWorkerFontProvider();

    /** 默认的Image Provider, 默认从images根目录读取图片 */
    public static final ImageProvider DEFAULT_IMAGE_PROVIDER = ImageResourceProvider.of("/images");

    /** 缓存CSS */
    static Map<String, CSSResolver> CSS_RESOLVERS = new ConcurrentHashMap<>(4);

    /**
     * 根据指定的CSS文件得到CSSResolver
     * 
     * @param cssFile
     * @return
     */
    public static CSSResolver getCSSResolver(String cssFile) {
        CSSResolver cssResolver = CSS_RESOLVERS.get(cssFile);
        if (cssResolver == null) {
            CssFilesImpl cssFiles = new CssFilesImpl();
            cssFiles.add(XMLWorkerHelper.getCSS(PdfUtils.class.getResourceAsStream(cssFile)));
            cssResolver = new StyleAttrCSSResolver(cssFiles);
            CSS_RESOLVERS.put(cssFile, cssResolver);
        }
        return cssResolver;
    }

    /**
     * 处理HTML内容,输出到生成的PDF文件.
     *
     * @param pdfFile PDF文件
     * @param html HTML内容
     */
    public static final void parseXHtml(File pdfFile, String html) {
        parseXHtml(pdfFile, HTMLDocument.builder().html(html).build());
    }

    /**
     * 生成PDF文档,输出到out输出流
     * 
     * @param out 输出流
     * @param html HTML内容
     */
    public static final void parseXHtml(OutputStream out, String html) {
        parseXHtml(out, HTMLDocument.builder().html(html).build());
    }


    /**
     * 生成PDF文档,输出到out输出流
     * @param out 输出流
     * @param html HTML内容
     */
    public static final void parseXHtmlRotate(OutputStream out, String html) {
        parseXHtmlRotate(out, HTMLDocument.builder().html(html).build());
    }

    /**
     * 生成PDF文件
     * 
     * @param pdfFile 要输出的PDF文件
     * @param doc HTML内容
     */
    @SneakyThrows
    public static final void parseXHtml(File pdfFile, HTMLDocument doc) {
        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            PdfUtils.parseXHtml(out, doc);
        }
    }

    /**
     * 生成PDF文档
     * 
     * @param out 输出流
     * @param doc HTML内容
     */
    @SneakyThrows
    public static final void parseXHtml(OutputStream out, HTMLDocument doc) {
        Document document = new Document(doc.getPageSize(), doc.getMarginLeft(), doc.getMarginRight(), doc.getMarginTop(),
                doc.getMarginBottom());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            parseXHtml(writer, document, out, doc);

        } finally {
            document.close();
        }

    }


    /**
     * 生成PDF文档（横板）
     * @param out 输出流
     * @param doc HTML内容
     */
    @SneakyThrows
    public static final void parseXHtmlRotate(OutputStream out, HTMLDocument doc) {
        Document document = new Document(PageSize.A4.rotate(), doc.getMarginLeft(), doc.getMarginRight(), doc.getMarginTop(), doc.getMarginBottom());

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            parseXHtml(writer, document, out, doc);

        } finally {
            document.close();
        }

    }

    /**
     * 
     * 生成PDF文档
     * 
     * @param writer PDF PdfWriter对象
     * @param document PDF Document对象
     * @param out 输出流
     * @param doc HTML内容
     */
    @SneakyThrows
    public static final void parseXHtml(final PdfWriter writer, final Document document, OutputStream out, HTMLDocument doc) {
        ByteArrayInputStream in = new ByteArrayInputStream(doc.getHtml().getBytes(doc.getCharset()));
        HtmlPipelineContext hpc = new HtmlPipelineContext(new CssAppliersImpl(doc.getFontProvider()));
        hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());
        hpc.setImageProvider(doc.getImageProvider());

        if (doc.getPageEvent() != null) {
            writer.setPageEvent(doc.getPageEvent());
        } else if (doc.getHeader() != null || doc.getFooter() != null) {
            writer.setPageEvent(HTMLPageEventHelper.of(doc.getHeader(), doc.getFooter(), doc.getCharset(), doc.getCssResolver(), hpc));
        }

        HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(document, writer));
        Pipeline<?> pipeline = new CssResolverPipeline(doc.getCssResolver(), htmlPipeline);
        XMLWorker worker = new XMLWorker(pipeline, true);
        XMLParser p = new XMLParser(true, worker, doc.getCharset());

        p.parse(in, doc.getCharset());
    }

    /**
     * 使用base64图片生成PDF
     *
     * @param out PDF输出流
     * @param html HTML内容
     * @param base64Images base64图片Map，key为图片在HTML中的引用名，value为base64编码的图片数据
     */
    @SneakyThrows
    public static final void parseXHtmlWithBase64Images(OutputStream out, String html, Map<String, String> base64Images) {
        // 创建支持base64的图片提供者
        Base64ImageProvider imageProvider = new Base64ImageProvider(DEFAULT_IMAGE_PROVIDER);

        // 添加所有base64图片
        if (base64Images != null) {
            base64Images.forEach(imageProvider::addBase64Image);
        }

        // 创建HTML文档
        HTMLDocument doc = HTMLDocument.builder()
                .html(html)
                .imageProvider(imageProvider)
                .build();

        // 生成PDF
        parseXHtml(out, doc);
    }

    /**
     * 使用base64图片生成PDF文件
     *
     * @param pdfFile PDF文件
     * @param html HTML内容
     * @param base64Images base64图片Map
     */
    @SneakyThrows
    public static final void parseXHtmlWithBase64Images(File pdfFile, String html, Map<String, String> base64Images) {
        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            parseXHtmlWithBase64Images(out, html, base64Images);
        }
    }
}
