package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 16/12/2025 10:27
 * Version : 1.0
 */
@TableName(value = "dataintegration.view_hsp_fee_account", autoResultMap = true)
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SubjectView {

    private String cust1Cd;

    private String cust1Nm;

    private String acctCd;

    private String acctNm;

    private String parAcctCd;

    private String parAcctNm;

    @TableField(value = "is_leaf")
    private boolean leaf;




}
