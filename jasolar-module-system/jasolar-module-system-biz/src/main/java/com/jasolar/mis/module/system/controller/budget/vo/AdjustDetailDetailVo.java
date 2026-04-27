package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jasolar.mis.module.system.controller.budget.validation.ConditionalMutuallyExclusiveNotNull;
import com.jasolar.mis.module.system.controller.budget.validation.ConditionalRequired;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Description: 调整申请明细
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "调整申请明细")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConditionalMutuallyExclusiveNotNull(
    field1 = "budgetSubjectCode",
    field2 = "erpAssetType",
    conditionField = "masterProjectCode",
    field1Name = "预算科目编码",
    field2Name = "ERP资产类型"
)
@ConditionalRequired(
    conditionField = "effectType",
    conditionValues = {"0", "2"},
    requiredFields = {"adjustAmountQ1", "adjustAmountQ2", "adjustAmountQ3", "adjustAmountQ4"},
    fieldNames = {"Q1调整金额", "Q2调整金额", "Q3调整金额", "Q4调整金额"}
)
@ConditionalRequired(
    conditionField = "effectType",
    conditionValues = {"1"},
    requiredFields = {"adjustAmountTotalInvestment"},
    fieldNames = {"全年合计调整金额"}
)
public class AdjustDetailDetailVo {

    /**
     * 调整明细行号（自动计算生成，不需要前端传入）
     * 格式：adjustYear@all@effectType@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 示例：2025@all@0@1@ORG001@NAN-NAN@NAN@NAN
     * 注意：effectType 为调整类型（0：预算调整-采购额，1：投资额调整，2：预算调整-付款额）
     */
    @JsonIgnore
    private String adjustDetailLineNo;

    /**
     * 调整年
     * 参考值：2025
     */
    @ApiModelProperty(value = "调整年", example = "2025", required = true)
    @NotBlank(message = "调整年不能为空")
    private String adjustYear;

    /**
     * 调整月
     * 参考值：10
     */
    @ApiModelProperty(value = "调整月", example = "10", required = true)
    @NotBlank(message = "调整月不能为空")
    private String adjustMonth;

    /**
     * 公司编码
     * 参考值：ZZ001
     */
    @ApiModelProperty(value = "公司编码", example = "ZZ001", required = false)
    private String company;

    /**
     * 部门编码
     * 参考值：Depart001
     */
    @ApiModelProperty(value = "部门编码", example = "Depart001", required = false)
    private String department;

    /**
     * 调整类型
     * 0：预算调整-采购额
     * 1：投资额调整
     * 2：预算调整-付款额
     */
    @ApiModelProperty(value = "调整类型，0：预算调整-采购额，1：投资额调整，2：预算调整-付款额", example = "0", required = true)
    @NotBlank(message = "调整类型不能为空")
    private String effectType;

    /**
     * 管理组织编码
     */
    @ApiModelProperty(value = "管理组织编码", required = true)
    @NotBlank(message = "管理组织编码不能为空")
    private String managementOrg;

    /**
     * 管理组织名称
     */
    @ApiModelProperty(value = "管理组织名称", required = false)
    private String managementOrgName;

    /**
     * 预算科目编码
     */
    @ApiModelProperty(value = "预算科目编码", required = false)
    private String budgetSubjectCode;

    /**
     * 预算科目名称
     */
    @ApiModelProperty(value = "预算科目名称", required = false)
    private String budgetSubjectName;

    /**
     * 主数据项目编码
     */
    @ApiModelProperty(value = "主数据项目编码", required = false)
    private String masterProjectCode;

    /**
     * 主数据项目名称
     */
    @ApiModelProperty(value = "主数据项目名称", required = false)
    private String masterProjectName;

    /**
     * ERP 资产类型
     */
    @ApiModelProperty(value = "ERP资产类型", required = false)
    private String erpAssetType;

    /**
     * 是否内部项目
     */
    @JsonIgnore
    private String isInternal;

    /**
     * 调整金额（Q1）
     * 当调整类型为0（预算调整-采购额）或2（预算调整-付款额）时必填
     */
    @ApiModelProperty(value = "第一季度调整金额，当调整类型为0或2时必填", example = "100.22", required = false)
    private BigDecimal adjustAmountQ1;

    /**
     * 调整金额（Q2）
     * 当调整类型为0（预算调整-采购额）或2（预算调整-付款额）时必填
     */
    @ApiModelProperty(value = "第二季度调整金额，当调整类型为0或2时必填", example = "100.22", required = false)
    private BigDecimal adjustAmountQ2;

    /**
     * 调整金额（Q3）
     * 当调整类型为0（预算调整-采购额）或2（预算调整-付款额）时必填
     */
    @ApiModelProperty(value = "第三季度调整金额，当调整类型为0或2时必填", example = "100.22", required = false)
    private BigDecimal adjustAmountQ3;

    /**
     * 调整金额（Q4）
     * 当调整类型为0（预算调整-采购额）或2（预算调整-付款额）时必填
     */
    @ApiModelProperty(value = "第四季度调整金额，当调整类型为0或2时必填", example = "100.22", required = false)
    private BigDecimal adjustAmountQ4;

    /**
     * 调整金额（全年合计）
     * 当调整类型为1（投资额调整）时必填
     */
    @ApiModelProperty(value = "全年合计调整金额，当调整类型为1时必填", example = "400.88", required = false)
    private BigDecimal adjustAmountTotalInvestment;

    /**
     * 币种
     * 参考值：CNY
     */
    @ApiModelProperty(value = "币种", example = "CNY", required = true)
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 调整原因
     */
    @ApiModelProperty(value = "调整原因", required = false)
    private String adjustReason;

    /**
     * 元数据（扩展信息）
     * 存储格式：{"text1": "123", "text2": "234", ...}
     */
    @ApiModelProperty(value = "元数据（扩展信息），存储格式：{\"text1\": \"123\", \"text2\": \"234\", ...}", required = false)
    private Map<String, String> metadata;
    
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

    public BigDecimal getAdjustAmountQ1() {
        return adjustAmountQ1 == null ? BigDecimal.ZERO : adjustAmountQ1;
    }

    public BigDecimal getAdjustAmountQ2() {
        return adjustAmountQ2 == null ? BigDecimal.ZERO : adjustAmountQ2;
    }

    public BigDecimal getAdjustAmountQ3() {
        return adjustAmountQ3 == null ? BigDecimal.ZERO : adjustAmountQ3;
    }

    public BigDecimal getAdjustAmountQ4() {
        return adjustAmountQ4 == null ? BigDecimal.ZERO : adjustAmountQ4;
    }
    
    /**
     * 自动计算并返回调整明细行号（不含季度，统一标记为 all）
     * 格式: adjustYear@all@effectType@isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 规则:
     *   - 单据级 isInternal 透传；masterProjectCode 为空（"NAN"）时默认 isInternal = "1"
     *   - 预算/项目/资产类型空值使用 NAN/NAN-NAN 占位
     */
    public String getAdjustDetailLineNo() {
        String masterProjectCode = getMasterProjectCode();
        String resolvedIsInternal = StringUtils.isBlank(this.isInternal) ? "1" : this.isInternal;
        if ("NAN".equals(masterProjectCode)) {
            resolvedIsInternal = "1";
        }
        
        // 不再按月份计算季度，统一用 all，实际季度由业务拆分时决定
        return this.adjustYear + "@all@" +
               this.effectType + "@" +
               resolvedIsInternal + "@" + this.managementOrg + "@" +
               getBudgetSubjectCode() + "@" + masterProjectCode + "@" +
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

