package com.jasolar.mis.module.system.domain.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 15:03
 * Version : 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@TableName(value = "system_user_group_project_r", autoResultMap = true)
public class SystemUserGroupProjectR extends BaseDO {


    @TableId(type = IdType.ASSIGN_ID)
    private Long id ;
    /** 用户组id,; */
    private Long userGroupId ;
    /** 项目id,; */
    private Long projectId ;

}
