package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科目控制层级视图
 * 功能：通过SUBJECT_INFO表，对所有ERP_ACCT_CD不为空的数据通过CUST1_CD和ACCT_CD向上追溯
 *      对应的控制层级CONTROL_LEVEL=1的CUST1_CD和ACCT_CD
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "V_SUBJECT_CONTROL_LEVEL", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectControlLevelView {

    /**
     * ERP科目编码（原始数据）
     */
    @TableField(value = "ERP_ACCT_CD")
    private String erpAcctCd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的CUST1_CD
     */
    @TableField(value = "CONTROL_CUST1_CD")
    private String controlCust1Cd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的ACCT_CD
     */
    @TableField(value = "CONTROL_ACCT_CD")
    private String controlAcctCd;
}

