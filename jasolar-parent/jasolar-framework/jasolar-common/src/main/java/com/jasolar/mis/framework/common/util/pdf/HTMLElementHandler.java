package com.jasolar.mis.framework.common.util.pdf;

import com.itextpdf.tool.xml.ElementHandler;
import com.itextpdf.tool.xml.ElementList;
import com.itextpdf.tool.xml.Writable;
import com.itextpdf.tool.xml.pipeline.WritableElement;

import lombok.Getter;

/**
 * 处理HTML对象
 * 
 * @author galuo
 * @date 2022/04/08
 */
public class HTMLElementHandler implements ElementHandler {

    /** 所有元素 */
    @Getter
    public final ElementList elements = new ElementList();

    @Override
    public void add(Writable w) {
        if (w instanceof WritableElement) {
            elements.addAll(((WritableElement) w).elements());
        }
    }

}
