package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户组ID查询结果VO
 * 
 * @author jasolar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupInfoVo {
    

    
    /** 用户组ID */
    private Long groupId;
    private String groupName;
}

