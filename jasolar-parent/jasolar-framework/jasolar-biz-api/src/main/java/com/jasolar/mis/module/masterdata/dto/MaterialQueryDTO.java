package com.jasolar.mis.module.masterdata.dto;

import java.util.List;

import com.jasolar.mis.framework.common.pojo.PageParam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 料号查询条件
 * 
 * @author galuo
 * @date 2025-04-09 18:58
 *
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialQueryDTO extends PageParam {

    @Schema(description = "料号（批量查询条件）")
    private List<String> materialCodeList;

    @Schema(description = "料号")
    private String code;

    @Schema(description = "品牌ID")
    private Long brandId;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "一阶类别")
    private String category1Code;

    @Schema(description = "二阶类别")
    private String category2Code;

    @Schema(description = "三阶类别")
    private String category3Code;

    @Schema(description = "品名(四阶类别)")
    private String category4Code;

    @Schema(description = "规格")
    private String specification;

    @Schema(description = "品名")
    private String category4Name;

    @Schema(description = "来源单号;询价单/决标单号")
    private String sourcingNo;

    @Schema(description = "有效状态;字典common_status:0有效,1无效.只要有一个价格有效则视为有效")
    private Integer status;

    @Schema(description = "默认查询所有 查询不为空的供应商传true")
    private Boolean supplierNotNull = Boolean.FALSE;

    @Schema(description = "默认查询false 是否仅查询类别料号")
    private Boolean categoryMaterialFlag = Boolean.FALSE;

    @Schema(description = "计量单位")
    private String unit;
}
