package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预算成员名称编码视图
 * 来源：DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "DATAINTEGRATION.VIEW_BUDGET_MEMBER_NAME_CODE", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetMemberNameCodeView {

    /**
     * 成员名称
     */
    @TableField(value = "MEMBER_NM")
    private String memberNm;

    /**
     * 成员编码
     */
    @TableField(value = "MEMBER_CD")
    private String memberCd;

    /**
     * 成员编码2
     */
    @TableField(value = "MEMBER_CD2")
    private String memberCd2;
}

