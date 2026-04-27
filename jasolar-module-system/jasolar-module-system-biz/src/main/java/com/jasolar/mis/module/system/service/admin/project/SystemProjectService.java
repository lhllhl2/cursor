package com.jasolar.mis.module.system.service.admin.project;

import com.jasolar.mis.module.system.controller.admin.project.vo.BindUserGroupVo;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectSearchVo;
import com.jasolar.mis.module.system.domain.project.SystemProject;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 11:20
 * Version : 1.0
 */
public interface SystemProjectService {

    List<SystemProject> getProjectList(ProjectSearchVo projectSearchVo);

    void bindUserGroup(BindUserGroupVo bindUserGroupVo);

    /**
     * 同步项目
     */
    void syncProjectToBusiness();


}
