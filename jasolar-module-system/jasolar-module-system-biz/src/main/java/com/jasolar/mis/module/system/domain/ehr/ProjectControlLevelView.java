package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目控制层级视图
 * 功能：通过PROJECT_CONTROL_R表，对所有PRJ_CD向上追溯对应的CONTROL_LEVEL=1的PRJ_CD
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@TableName(value = "V_PROJECT_CONTROL_LEVEL", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectControlLevelView {

    /**
     * 原始项目编码
     */
    @TableField(value = "PRJ_CD")
    private String prjCd;

    /**
     * 向上追溯找到的CONTROL_LEVEL=1的项目编码
     */
    @TableField(value = "CONTROL_PRJ_CD")
    private String controlPrjCd;
}

