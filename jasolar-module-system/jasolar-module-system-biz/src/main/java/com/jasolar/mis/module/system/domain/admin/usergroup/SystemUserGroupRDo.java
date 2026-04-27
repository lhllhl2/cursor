package com.jasolar.mis.module.system.domain.admin.usergroup;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 23/07/2025 14:15
 * Version : 1.0
 */
@TableName(value = "system_user_group_r", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserGroupRDo extends BaseDO {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id ;

    /** 用户id */
    private Long userId ;

    /** 用户组id */
    private Long groupId ;

    /** 类型 */
    private String type ;

}
