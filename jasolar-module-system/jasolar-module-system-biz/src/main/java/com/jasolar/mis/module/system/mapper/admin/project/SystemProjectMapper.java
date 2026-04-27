package com.jasolar.mis.module.system.mapper.admin.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.project.vo.ProjectSearchVo;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.resp.ProjectExceptResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 项目信息 Mapper 接口
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Mapper
public interface SystemProjectMapper extends BaseMapperX<SystemProject> {
    
    /**
     * 查询当前 Schema
     */
    @Select("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL")
    String getCurrentSchema();


    default List<SystemProject> getProjectList(ProjectSearchVo projectSearchVo){
        LambdaQueryWrapper<SystemProject> queryWrapper =
                new LambdaQueryWrapper<SystemProject>();
        if(StringUtils.hasLength(projectSearchVo.getProjectCode())){
            queryWrapper.like(SystemProject::getProjectCode, projectSearchVo.getProjectCode().trim());
        }
        if(StringUtils.hasLength(projectSearchVo.getProjectName())){
            queryWrapper.like(SystemProject::getProjectName, projectSearchVo.getProjectName().trim());
        }
        return selectList(queryWrapper);
    }


   default List<SystemProject> getByCds(List<String> prjCdSet){
        LambdaQueryWrapper<SystemProject> queryWrapper =
                new LambdaQueryWrapper<SystemProject>();
        return selectList(queryWrapper.in(SystemProject::getProjectCode, prjCdSet));
   }

    List<ProjectExceptResp> selectExceptData();

}

