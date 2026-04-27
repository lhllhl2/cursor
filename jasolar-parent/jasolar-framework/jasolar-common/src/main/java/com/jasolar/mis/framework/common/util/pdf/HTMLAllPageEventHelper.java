package com.jasolar.mis.framework.common.util.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.ElementHandlerPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

/**
 * 默认配置, 全部页的页眉页脚都一样
 * 
 * @author galuo
 * @date 2022/08/11
 */
public class HTMLAllPageEventHelper extends HTMLPageEventHelper {

    /** 页眉 */
    private HTMLHeader header;
    /** 页脚 */
    private HTMLFooter footer;

    @Override
    public HTMLHeader getHeader(int pageNumber) {
        return header;
    }

    @Override
    public HTMLFooter getFooter(int pageNumber) {
        return footer;
    }

    /**
     * 初始化页眉页脚. 页眉和页脚必须至少有一个不为null
     * 
     * @param header
     * @param footer
     */
    protected void initialize(@Nullable final HTMLHeader header, @Nullable final HTMLFooter footer, final Charset charset,
            final CSSResolver cssResolver, final HtmlPipelineContext hpc) {
        if (header == null && footer == null) {
            throw new IllegalStateException("页眉和页脚不能同时为null");
        }

        this.header = header;
        this.footer = footer;

        if (header != null) {
            HTMLElementHandler headerHandler = new HTMLElementHandler();
            try {
                Pipeline<?> headerPipeline = new CssResolverPipeline(cssResolver,
                        new HtmlPipeline(hpc, new ElementHandlerPipeline(headerHandler, null)));
                XMLWorker worker = new XMLWorker(headerPipeline, true);
                XMLParser p = new XMLParser(true, worker, charset);
                p.parse(new ByteArrayInputStream(header.html.getBytes(charset)), charset);
                header.setElements(headerHandler.getElements());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (footer != null) {
            HTMLElementHandler footerHandler = new HTMLElementHandler();
            try {

                Pipeline<?> headerPipeline = new CssResolverPipeline(cssResolver,
                        new HtmlPipeline(hpc, new ElementHandlerPipeline(footerHandler, null)));
                XMLWorker worker = new XMLWorker(headerPipeline, true);
                XMLParser p = new XMLParser(true, worker, charset);
                p.parse(new ByteArrayInputStream(footer.html.getBytes(charset)), charset);
                footer.setElements(footerHandler.getElements());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
