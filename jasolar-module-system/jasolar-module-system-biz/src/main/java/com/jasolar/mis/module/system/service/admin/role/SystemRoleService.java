package com.jasolar.mis.module.system.service.admin.role;

import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleEditVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RolePageVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleSaveVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserByRoleResp;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleDo;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:36
 * Version : 1.0
 */
public interface SystemRoleService {


    /**
     * 保存
     * @param roleSaveVo
     */
    void saveRole(RoleSaveVo roleSaveVo);

    /**
     * 分页查询
     * @param rolePageVo
     * @return
     */
    PageResult<SystemRoleDo> rolePage(RolePageVo rolePageVo);

    /**
     * 删除
     * @param primaryParam
     */
    void delete(PrimaryParam primaryParam);

    /**
     * 编辑
     * @param roleEditVo
     */
    void editRole(RoleEditVo roleEditVo);
    /**
     * 根据角色ID查询菜单树
     * @param roleId 角色ID
     * @return 菜单树列表
     */
    List<MenuRespVO> getMenuTreeByRoleId(Long roleId);

    /**
     * 根绝角色id查询用户组
     * @param primaryParam
     */
    List<GroupUserByRoleResp> searchUserGroupByRole(PrimaryParam primaryParam);
}
