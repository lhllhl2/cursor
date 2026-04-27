package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Description: 查询明细信息VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel("查询明细信息VO")
public class QueryDetailDetailVo {


    @ApiModelProperty(value = "查询年份", example = "2025")
    @NotBlank(message = "查询年份不能为空")
    private String queryYear;

    @ApiModelProperty(value = "查询月份", example = "01")
    @NotBlank(message = "查询月份不能为空")
    private String queryMonth;

    @ApiModelProperty(value = "公司编码", example = "COMP-001")
    private String company;

    @ApiModelProperty(value = "部门编码", example = "DEPT-001")
    private String department;

    @ApiModelProperty(value = "管理组织编码", example = "MGT-ORG-001")
    @NotBlank(message = "管理组织编码不能为空")
    private String managementOrg;

    @ApiModelProperty(value = "预算科目编码", example = "SUBJ-001")
    private String budgetSubjectCode;

    @ApiModelProperty(value = "主数据项目编码", example = "PROJ-001")
    private String masterProjectCode;

    @ApiModelProperty(value = "ERP 资产类型", example = "ASSET-TYPE-001")
    private String erpAssetType;

    @ApiModelProperty(value = "集团内/集团外(1:集团内,0:集团外)", example = "1")
    @NotBlank(message = "集团内/集团外标识不能为空")
    private String isInternal;

    @ApiModelProperty(value = "币种", example = "CNY")
    @NotBlank(message = "币种不能为空")
    private String currency;

    @ApiModelProperty(value = "调整类型（仅用于调整单查询），0：预算调整-采购额，1：投资额调整，2：预算调整-付款额", example = "0", required = false)
    private String effectType;

    @ApiModelProperty(value = "元数据（扩展信息），存储格式：{\"text1\": \"123\", \"text2\": \"234\", ...}", required = false)
    private Map<String, String> metadata;

    @JsonIgnore
    private String detailLineNo;

    /**
     * 重写 budgetSubjectCode 的 getter，将空值转换为 "NAN-NAN"
     */
    public String getBudgetSubjectCode() {
        return StringUtils.isBlank(budgetSubjectCode) ? "NAN-NAN" : budgetSubjectCode;
    }
    
    /**
     * 重写 masterProjectCode 的 getter，将空值转换为 "NAN"
     */
    public String getMasterProjectCode() {
        return StringUtils.isBlank(masterProjectCode) ? "NAN" : masterProjectCode;
    }
    
    /**
     * 重写 erpAssetType 的 getter，将空值转换为 "NAN"
     */
    public String getErpAssetType() {
        return StringUtils.isBlank(erpAssetType) ? "NAN" : erpAssetType;
    }

    /**
     * 自动计算并返回查询明细行号
     * 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 规则:
     *   - 不再包含年月（季度），以便不同季度的查询可以关联到需求单
     *   - budgetSubjectCode 为空时默认为 "NAN-NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空时默认为 "NAN"（由 getter 方法处理）
     *   - erpAssetType 为空时默认为 "NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空（即 "NAN"）时，默认 isInternal = "1"（部门预算）
     */
    public String getDetailLineNo() {
        String masterProjectCode = getMasterProjectCode();
        // 默认按部门预算维度处理：空值或项目为空时均置为 "1"
        String resolvedIsInternal = StringUtils.isBlank(this.isInternal) ? "1" : this.isInternal;
        if ("NAN".equals(masterProjectCode)) {
            resolvedIsInternal = "1";
        }
        
        // 拼接返回 - 使用 getter 方法获取值（自动处理空值和默认值）
        // 注意：不再包含 queryYear 和 quarter
        return resolvedIsInternal + "@" + 
               this.managementOrg + "@" + 
               getBudgetSubjectCode() + "@" + 
               masterProjectCode + "@" + 
               getErpAssetType();
    }

    /**
     * 根据月份计算季度
     * @param month 月份字符串（1-12）
     * @return 季度字符串（q1-q4，异常情况返回q0）
     */
    private String calculateQuarter(String month) {
        if (StringUtils.isBlank(month)) {
            return "q0";
        }
        try {
            int m = Integer.parseInt(month);
            if (m >= 1 && m <= 3) return "q1";
            if (m >= 4 && m <= 6) return "q2";
            if (m >= 7 && m <= 9) return "q3";
            if (m >= 10 && m <= 12) return "q4";
            return "q0";
        } catch (NumberFormatException e) {
            return "q0";
        }
    }
}

