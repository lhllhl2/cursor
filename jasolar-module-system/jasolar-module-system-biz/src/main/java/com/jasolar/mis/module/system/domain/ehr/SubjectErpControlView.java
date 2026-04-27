package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科目ERP控制层级视图
 * 功能：通过SUBJECT_INFO表，对所有ERP_ACCT_CD不为空的数据，展示CUST1_CD和ACCT_CD，
 *      并向上追溯对应的控制层级CONTROL_LEVEL=1的ACCT_CD作为CONTROL_ACCT_CD
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "V_SUBJECT_ERP_CONTROL", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectErpControlView {

    /**
     * ERP科目编码（原始数据，不为空）
     */
    @TableField(value = "ERP_ACCT_CD")
    private String erpAcctCd;

    /**
     * 客户1编码（原始数据）
     */
    @TableField(value = "CUST1_CD")
    private String cust1Cd;

    /**
     * 科目编码（原始数据）
     */
    @TableField(value = "ACCT_CD")
    private String acctCd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的ACCT_CD
     */
    @TableField(value = "CONTROL_ACCT_CD")
    private String controlAcctCd;
}

