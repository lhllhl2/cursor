package com.jasolar.mis.framework.common.util.pdf;

import com.itextpdf.text.Element;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 页眉
 * 
 * @author galuo
 * @date 2022/04/09
 */
@Data
@NoArgsConstructor
@SuperBuilder
public class HTMLHeader extends AbstractHTML {

    /** 行距 */
    @Builder.Default
    protected float leading = 1.0F;

    /** 对齐方式,默认居中对齐 */
    @Builder.Default
    protected int alignment = Element.ALIGN_CENTER;

}
