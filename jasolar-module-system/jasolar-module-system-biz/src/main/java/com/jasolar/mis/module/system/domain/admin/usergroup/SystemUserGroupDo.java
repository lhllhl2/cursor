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
 * Date : 23/07/2025 11:14
 * Version : 1.0
 */
@TableName(value = "system_user_group", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserGroupDo extends BaseDO {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id ;

    /** 用户组编码 */
    private String code ;

    /** 用户组名称 */
    private String name ;

    /** 用户组类型【1.菜单权限 2.用户组权限】 */
    private String type ;

    /** 说明 */
    private String remark ;


}
