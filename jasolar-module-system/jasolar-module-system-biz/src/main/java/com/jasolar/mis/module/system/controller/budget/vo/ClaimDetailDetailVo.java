package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jasolar.mis.module.system.controller.budget.validation.ConditionalMutuallyExclusiveNotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Description: 付款/报销申请明细
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@ApiModel(description = "付款/报销申请明细")
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
public class ClaimDetailDetailVo {

    /**
     * 报销/付款明细行号（自动计算生成，不需要前端传入）
     * 格式：isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 示例：1@ORG001@NAN-NAN@NAN@NAN
     * 注意：不再包含年月（季度），以便不同季度的付款单可以关联到需求单或合同单
     */
    @JsonIgnore
    private String claimDetailLineNo;

    /**
     * 报销/付款年
     * 参考值：2025
     */
    @ApiModelProperty(value = "报销/付款年", example = "2025", required = true)
    @NotBlank(message = "报销/付款年不能为空")
    private String claimYear;

    /**
     * 报销/付款月
     * 参考值：10
     */
    @ApiModelProperty(value = "报销/付款月", example = "10", required = true)
    @NotBlank(message = "报销/付款月不能为空")
    private String claimMonth;

    /**
     * 实际发生年度
     * 可选，参考值：2025
     */
    @ApiModelProperty(value = "实际发生年度", example = "2025", required = false)
    private String actualYear;

    /**
     * 实际发生月份
     * 可选，参考值：10
     */
    @ApiModelProperty(value = "实际发生月份", example = "10", required = false)
    private String actualMonth;

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
    @ApiModelProperty(value = "是否在集团内，0-否，1-是", example = "1", required = true)
    @NotBlank(message = "是否在集团内不能为空")
    private String isInternal;

    /**
     * 实际发生金额
     * 参考值：100.22
     */
    @ApiModelProperty(value = "实际发生金额", example = "100.22", required = true)
    @NotNull(message = "实际发生金额不能为空")
    private BigDecimal actualAmount;

    /**
     * 币种
     * 参考值：CNY
     */
    @ApiModelProperty(value = "币种", example = "CNY", required = true)
    @NotBlank(message = "币种不能为空")
    private String currency;

    /**
     * 关联需求明细
     */
    @ApiModelProperty(value = "关联需求明细", required = false)
    private List<ClaimApplyDetailVo> claimApplyDetails;

    /**
     * 设置报销申请明细关联信息列表，并自动为每个子项设置父级引用
     */
    public void setClaimApplyDetails(List<ClaimApplyDetailVo> claimApplyDetails) {
        this.claimApplyDetails = claimApplyDetails;
        // 为每个子项设置父级引用，以便自动计算 demandDetailLineNo
        if (claimApplyDetails != null) {
            for (ClaimApplyDetailVo detail : claimApplyDetails) {
                if (detail != null) {
                    detail.setParentDetail(this);
                }
            }
        }
    }

    /**
     * 关联合同明细
     */
    @ApiModelProperty(value = "关联合同明细", required = false)
    private List<ClaimContractDetailVo> claimContractDetails;

    /**
     * 设置报销合同明细关联信息列表，并自动为每个子项设置父级引用
     */
    public void setClaimContractDetails(List<ClaimContractDetailVo> claimContractDetails) {
        this.claimContractDetails = claimContractDetails;
        // 为每个子项设置父级引用，以便自动计算 contractDetailLineNo
        if (claimContractDetails != null) {
            for (ClaimContractDetailVo detail : claimContractDetails) {
                if (detail != null) {
                    detail.setParentDetail(this);
                }
            }
        }
    }

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
    
    /**
     * 自动计算并返回报销/付款明细行号
     * 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 规则:
     *   - 不再包含年月（季度），以便不同季度的付款单可以关联到需求单或合同单
     *   - budgetSubjectCode 为空时默认为 "NAN-NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空时默认为 "NAN"（由 getter 方法处理）
     *   - erpAssetType 为空时默认为 "NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空（即 "NAN"）时，默认 isInternal = "1"
     */
    public String getClaimDetailLineNo() {
        String masterProjectCode = getMasterProjectCode();
        // 空值或项目为空都按部门预算默认 isInternal=1
        String resolvedIsInternal = StringUtils.isBlank(this.isInternal) ? "1" : this.isInternal;
        if ("NAN".equals(masterProjectCode)) {
            resolvedIsInternal = "1";
        }
        
        // 拼接返回 - 使用 getter 方法获取值（自动处理空值和默认值）
        // 注意：不再包含 claimYear 和 quarter
        return resolvedIsInternal + "@" + this.managementOrg + "@" + 
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


