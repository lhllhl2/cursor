package com.jasolar.mis.module.system.controller.admin.usergroup.resp;

import com.jasolar.mis.module.system.domain.admin.usergroup.SystemUserGroupDo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 29/07/2025 11:38
 * Version : 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupTreeNodeResp {

    private Long id ;

    /** 用户组编码 */
    private String code ;

    /** 用户组名称 */
    private String name ;

    /** 用户组类型【1.菜单权限 2.用户组权限】 */
    private String type ;

    /** 说明 */
    private String remark ;
    // 是否启用
    private Boolean isEnable;

    public UserGroupTreeNodeResp(SystemUserGroupDo userGroupDo) {
        this.id = userGroupDo.getId();
        this.code = userGroupDo.getCode();
        this.name = userGroupDo.getName();
        this.type = userGroupDo.getType();
        this.remark = userGroupDo.getRemark();
    }


}
