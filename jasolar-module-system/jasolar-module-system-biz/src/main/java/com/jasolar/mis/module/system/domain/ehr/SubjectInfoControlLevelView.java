package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 科目控制层级视图（用于科目配置页面展示）
 *
 * 在 V_SUBJECT_INFO_CONTROL_LEVEL 视图上新增：
 * - controlAcctCd：控制层级科目编码
 * - controlAcctNm：控制层级科目名称
 */
@TableName(value = "V_SUBJECT_INFO_CONTROL_LEVEL", autoResultMap = true)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubjectInfoControlLevelView extends SubjectInfo {

    @TableField(value = "CONTROL_ACCT_CD")
    private String controlAcctCd;

    @TableField(value = "CONTROL_ACCT_NM")
    private String controlAcctNm;
}

