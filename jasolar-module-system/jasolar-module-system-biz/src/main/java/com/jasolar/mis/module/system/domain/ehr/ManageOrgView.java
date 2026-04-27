package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 29/12/2025 18:03
 * Version : 1.0
 */
@TableName(value = "dataintegration.view_hsp_entity", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageOrgView {

    /**
     * 组织ID
     */
    @TableId(value = "org_id")
    private Long id;

    /**
     * 组织名称
     */
    @TableField(value = "org_nm")
    private String name;
    /**
     * 组织编码
     */
    @TableField(value = "org_cd")
    private String code;
    /**
     * 父级组织编码，顶级组织为空或0
     */
    @TableField(value = "par_cd")
    private String pCode;

    @TableField(value = "par_nm")
    private String pName;

    /**
     * 是否末级
     */
    @TableField(value = "is_leaf")
    private Boolean isLastLvl;
}
