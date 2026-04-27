package com.jasolar.mis.module.system.mapper.admin.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.project.SystemUserGroupProjectR;
import com.jasolar.mis.module.system.resp.ProjectUserGroupResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 30/12/2025 15:01
 * Version : 1.0
 */
@Mapper
public interface SystemUserGroupProjectRMapper extends BaseMapperX<SystemUserGroupProjectR>{

        default void deleteByProjectIds(List<Long> projectIds){
            LambdaQueryWrapper<SystemUserGroupProjectR> lw = new LambdaQueryWrapper<SystemUserGroupProjectR>()
                    .in(SystemUserGroupProjectR::getProjectId, projectIds);
            delete(lw);
        }

   default List<Long> getGroupIdsByProjectId(Long projectId){

            LambdaQueryWrapper<SystemUserGroupProjectR> lw = new LambdaQueryWrapper<SystemUserGroupProjectR>()
                    .eq(SystemUserGroupProjectR::getProjectId, projectId)
                    .select(SystemUserGroupProjectR::getUserGroupId);

            return selectObjs(lw).stream()
                    .map(obj -> {
                        if (obj instanceof BigDecimal) {
                            return ((BigDecimal) obj).longValue();
                        } else if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return (Long) obj;
                    })
                    .toList();
   }


    IPage<ProjectUserGroupResp> getRelationPage(IPage<ProjectUserGroupResp>  page);

    void insertBatchExsit(@Param("rs") List<SystemUserGroupProjectR> rs);
}
