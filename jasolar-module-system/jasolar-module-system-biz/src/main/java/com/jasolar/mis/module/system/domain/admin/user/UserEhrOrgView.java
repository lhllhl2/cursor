package com.jasolar.mis.module.system.domain.admin.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户EHR组织关联视图
 * 
 * @author jasolar
 */
@TableName(value = "V_USER_EHR_ORG", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEhrOrgView {

    /**
     * 用户名
     */
    @TableField(value = "USER_NAME")
    private String userName;

    /**
     * 管理组织编码
     */
    @TableField(value = "MORG_CODE")
    private String morgCode;

    /**
     * EHR组织编码
     */
    @TableField(value = "EHR_CD")
    private String ehrCd;

    /**
     * 控制层级1的EHR组织编码
     * 通过向上追溯EHR_ORG_MANAGE_R找到CONTROL_LEVEL=1的父级EHR_CD
     */
    @TableField(value = "CONTROL_EHR_CD")
    private String controlEhrCd;
}

