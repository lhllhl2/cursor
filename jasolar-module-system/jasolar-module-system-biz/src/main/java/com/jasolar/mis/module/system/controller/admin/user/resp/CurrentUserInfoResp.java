package com.jasolar.mis.module.system.controller.admin.user.resp;

import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jasolar.mis.module.system.controller.admin.role.resp.RoleSimpleResp;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.CurrentUserGroupResp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 01/08/2025 11:17
 * Version : 1.0
 */
@Data
@Builder
public class CurrentUserInfoResp {

    private Long id;

    private String userName;

    private String displayName;

    private String email;

    private String post;


    private List<CurrentUserGroupResp> userGroupList;

    private List<RoleSimpleResp> roleSimpleRespList;

    private List<MenuRespVO> menuList;

    private List<MenuRespVO> buttonList;

}
