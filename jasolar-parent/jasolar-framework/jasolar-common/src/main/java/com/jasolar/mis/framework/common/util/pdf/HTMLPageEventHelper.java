package com.jasolar.mis.framework.common.util.pdf;

import java.nio.charset.Charset;

import javax.annotation.Nullable;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * 通过HTML生成页眉页脚
 * 
 * @author galuo
 * @date 2022/04/08
 */
@Getter
@Setter
public abstract class HTMLPageEventHelper extends PdfPageEventHelper {

    /**
     * 全部页一样的页眉页脚
     * 
     * @param header 页眉
     * @param footer 页脚
     * @param charset 字符编码
     * @param cssResolver CSS处理
     * @param hpc Context
     * @return
     */
    public static HTMLPageEventHelper of(HTMLHeader header, HTMLFooter footer, final Charset charset, CSSResolver cssResolver,
            HtmlPipelineContext hpc) {
        HTMLAllPageEventHelper e = new HTMLAllPageEventHelper();
        e.initialize(header, footer, charset, cssResolver, hpc);
        return e;
    }

    /** 防止被外部类初始化 */
    protected HTMLPageEventHelper() {
        super();
    }

    /**
     * 得到指定页的页眉
     * 
     * @param pageNumber 页数
     * @return
     */
    @Nullable
    public abstract HTMLHeader getHeader(int pageNumber);

    /**
     * 得到指定页的页脚
     * 
     * @param pageNumber 页数
     * @return
     */
    @Nullable
    public abstract HTMLFooter getFooter(int pageNumber);

    @Override
    @SneakyThrows
    public void onEndPage(PdfWriter writer, Document document) {
        super.onEndPage(writer, document);
        HTMLHeader header = this.getHeader(document.getPageNumber());
        if (header != null) {
            ColumnText headerText = new ColumnText(writer.getDirectContent());
            for (Element e : header.getElements()) {
                headerText.addElement(e);
            }
            Rectangle page = document.getPageSize();
            headerText.setSimpleColumn(page.getLeft(header.marginLeft), page.getBottom(header.marginBottom),
                    page.getRight(header.marginRight), page.getTop(header.marginTop), header.leading, header.alignment);

            headerText.go();
        }

        HTMLFooter footer = this.getFooter(document.getPageNumber());
        if (footer != null) {
            ColumnText footerText = new ColumnText(writer.getDirectContent());
            for (Element e : footer.getElements()) {
                footerText.addElement(e);
            }
            Rectangle page = document.getPageSize();
            footerText.setSimpleColumn(page.getLeft(footer.marginLeft), page.getBottom(footer.marginBottom),
                    page.getRight(footer.marginRight), page.getTop(footer.marginTop), footer.leading, footer.alignment);
            footerText.go();
        }
    }

}
