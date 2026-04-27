package com.jasolar.mis.module.system.controller.budget.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 报销关联合同明细
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimContractDetailVo {

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 合同明细行号（自动计算生成，不需要前端传入）
     * 格式：isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 示例：0@ORG001@NAN-NAN@NAN@NAN
     * 注意：不再包含年月（季度），以便不同季度的付款单可以关联到合同单
     */
    @JsonIgnore
    private String contractDetailLineNo;

    /**
     * 父级报销/付款明细（用于自动计算 contractDetailLineNo，不序列化）
     */
    @JsonIgnore
    @ToString.Exclude
    private ClaimDetailDetailVo parentDetail;

    /**
     * 自动计算并返回合同明细行号
     * 格式: isInternal@managementOrg@budgetSubjectCode@masterProjectCode@erpAssetType
     * 规则:
     *   - 不再包含年月（季度），以便不同季度的付款单可以关联到合同单
     *   - budgetSubjectCode 为空时默认为 "NAN-NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空时默认为 "NAN"（由 getter 方法处理）
     *   - erpAssetType 为空时默认为 "NAN"（由 getter 方法处理）
     *   - masterProjectCode 为空（即 "NAN"）时，默认 isInternal = "1"（部门预算）
     */
    public String getContractDetailLineNo() {
        if (parentDetail == null) {
            return contractDetailLineNo; // 如果没有父级，返回原始值
        }
        
        String masterProjectCode = parentDetail.getMasterProjectCode();
        // 默认按部门预算维度处理：空值或项目为空时均置为 "1"
        String resolvedIsInternal = StringUtils.isBlank(parentDetail.getIsInternal()) ? "1" : parentDetail.getIsInternal();
        if ("NAN".equals(masterProjectCode)) {
            resolvedIsInternal = "1";
        }
        
        // 拼接返回 - 使用 getter 方法获取值（自动处理空值和默认值）
        // 注意：不再包含 claimYear 和 quarter
        return resolvedIsInternal + "@" + parentDetail.getManagementOrg() + "@" + 
               parentDetail.getBudgetSubjectCode() + "@" + masterProjectCode + "@" + 
               parentDetail.getErpAssetType();
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

