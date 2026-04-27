package com.jasolar.mis.framework.common.util.pdf;

import com.itextpdf.tool.xml.ElementList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * HTML对象, 主要用于定义页眉页脚
 * 
 * @author galuo
 * @date 2022/04/09
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractHTML {

    /** HTML内容 */
    protected String html;

    /** 页面左边距 */
    protected float marginLeft;
    /** 页面右边距 */
    protected float marginRight;
    /** 上边距 */
    protected float marginTop;
    /** 下边距 */
    protected float marginBottom;

    /** HTML解析到的对象 */
    protected ElementList elements;

}
