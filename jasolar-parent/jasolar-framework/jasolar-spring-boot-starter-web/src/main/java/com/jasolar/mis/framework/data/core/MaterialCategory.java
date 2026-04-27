package com.jasolar.mis.framework.data.core;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物料分类, 缓存中精简字段, 仅存少量字段
 * 
 * @author galuo
 * @date 2025-04-09 12:08
 *
 */
@SuppressWarnings("serial")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialCategory implements Serializable {

    /**
     * 序列号;在上级分类下按照序列号递增的序号：四阶类别/品名码 (001-999)，三阶类别码 (001-999)，二阶类别码 (01-99)，一阶类别码 (01-99)
     */
    private String serialNo;

    /**
     * 编号;按照料号生成规则，将每阶类别的序号合并生成的唯一编码. 品名的格式为：1122333-444。前两位是一阶类别码。3、4位二阶类别码；5、6、7位三阶类别码；八位固定为短横线；9，10，11位四阶类别码,也即品名。一阶只有2位，二阶4位，三阶7位，四阶11位
     */
    private String code;

    // /** 父类CODE */
    // private String parentCode;
    /**
     * 名称
     */
    private String name;
    /**
     * 阶别;1一阶，2二阶，3三阶，4四阶（也即品名）
     */
    private Integer lv;

    /**
     * 一阶分类编码;用于平铺所有上级分类。lv=1时，此字段等于code
     */
    private String lv1Code;

    /** 一阶分类名称 */
    private String lv1Name;

    /**
     * 二阶分类编码;用于平铺所有上级分类。lv=2时，此字段等于code
     */
    private String lv2Code;
    /** 二阶分类名称 */
    private String lv2Name;
    /**
     * 三阶分类编码;用于平铺所有上级分类。lv=3时，此字段等于code
     */
    private String lv3Code;
    /** 三阶分类名称 */
    private String lv3Name;

    /**
     * 有效状态;字典common_status
     */
    private Integer status;

    /**
     * 料号属性;字典material_nature.
     */
    private String materialNature;

    /**
     * 科目代码;字典material_category_account_code
     */
    private String accountCode;
}
