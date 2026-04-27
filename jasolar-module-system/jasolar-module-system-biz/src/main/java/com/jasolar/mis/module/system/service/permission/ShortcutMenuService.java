package com.jasolar.mis.module.system.service.permission;

import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuRespVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuSaveReqVO;

import java.util.List;

/**
 * 用户快捷菜单 Service 接口
 *
 * @author 管理员
 */
public interface ShortcutMenuService {

    /**
     * 创建用户快捷菜单
     *
     * @return 编号
     */
    Boolean saveShortcutMenu(List<ShortcutMenuSaveReqVO> createReqVOs);


    /**
     * 获得用户快捷菜单分页
     *
     * @return 用户快捷菜单分页
     */
    List<ShortcutMenuRespVO> listShortcutMenu();

}