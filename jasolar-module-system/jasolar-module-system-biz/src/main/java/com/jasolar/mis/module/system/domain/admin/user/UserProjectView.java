package com.jasolar.mis.module.system.domain.admin.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户项目关联视图
 * 
 * @author jasolar
 */
@TableName(value = "V_USER_PROJECT", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectView {

    /**
     * 用户名
     */
    @TableField(value = "USER_NAME")
    private String userName;

    /**
     * 项目编码
     */
    @TableField(value = "PROJECT_CODE")
    private String projectCode;
}

