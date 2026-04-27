package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import lombok.Data;

/**
 * 用户组ID查询结果VO
 * 
 * @author jasolar
 */
@Data
public class UserGroupIdVo {
    
    /** 用户ID */
    private Long userId;
    
    /** 用户组ID */
    private Long groupId;

    /** 用户组名称 */
    private String groupName;
}

