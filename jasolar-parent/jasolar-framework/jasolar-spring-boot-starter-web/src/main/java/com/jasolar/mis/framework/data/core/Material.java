package com.jasolar.mis.framework.data.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物料数据
 * 
 * @author galuo
 * @date 2025-04-09 20:52
 *
 */
@SuppressWarnings("serial")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Material implements Serializable {

    @Schema(description = "料号ID")
    private Long id;

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

    @Schema(description = "一阶分类名称")
    private String category1Name;

    @Schema(description = "二阶分类名称")
    private String category2Name;

    @Schema(description = "三阶分类名称")
    private String category3Name;

    @Schema(description = "品名")
    private String category4Name;

    @Schema(description = "图片附件ID", example = "16988")
    private Long imgId;

    @Schema(description = "料号属性;字典material_nature.CATEGORY-MATERIAL:类别料号, SPECIFIC-MATERIAL:具体料号,SPECIAL-MATERIAL:专用料号. 如果是类别料号,则料号就等于4阶品类,数据来源于品名创建",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nature;

    @Schema(description = "料号分类;字典material_type", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String type;

    @Schema(description = "规格", requiredMode = Schema.RequiredMode.REQUIRED)
    private String specification;

    @Schema(description = "规格描述")
    private String specificationDesc;

    @Schema(description = "开票规格")
    private String invSpecification;

    @Schema(description = "计量单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Schema(description = "产地", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productOrigin;

    @Schema(description = "使用范围")
    private String usageScope;

    @Schema(description = "退税否;1是,0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean refundable;

    @Schema(description = "是否客供料号;1是,0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean clientProvided;

    @Schema(description = "是否定制件;1是,0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean customized;

    @Schema(description = "是否HUB料号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hub;

    @Schema(description = "参考交期（天）")
    private Integer deliveryDays;

    @Schema(description = "使用期限(月);根据最近一次核价的【价格有效期】进行计算")
    private BigDecimal duration;

    @Schema(description = "来源单号;询价单/决标单号")
    private String sourcingNo;

    @Schema(description = "最新决标/询价申请日期")
    private LocalDateTime inquiryDate;

    @Schema(description = "供应日期起")
    private LocalDate supplyStartDate;

    @Schema(description = "供应日期止")
    private LocalDate supplyEndDate;

    @Schema(description = "议价方式;字典")
    private String negotiationMethod;

    @Schema(description = "议价部门;部门CODE")
    private String negotiationDeptCode;

    @Schema(description = "统购部门;部门CODE")
    private String centralizedPurchasingDeptCode;

    @Schema(description = "库存方式;字典")
    private String inventoryMethod;

    @Schema(description = "物料归属;字典")
    private String ownership;

    @Schema(description = "采购方式")
    private String purchasingMethod;

}
