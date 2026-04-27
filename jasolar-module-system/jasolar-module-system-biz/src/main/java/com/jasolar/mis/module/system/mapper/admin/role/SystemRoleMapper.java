package com.jasolar.mis.module.system.mapper.admin.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.role.resp.RoleSimpleResp;
import com.jasolar.mis.module.system.controller.admin.role.vo.RolePageVo;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleDo;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:45
 * Version : 1.0
 */
@Mapper
public interface SystemRoleMapper extends BaseMapperX<SystemRoleDo> {


   default boolean replicateName(String name){

       LambdaQueryWrapper<SystemRoleDo> eq = new LambdaQueryWrapper<SystemRoleDo>()
               .eq(SystemRoleDo::getName, name)
               .eq(SystemRoleDo::getDeleted, 0);

      return selectCount(eq) > 0;


   }

    default boolean replicateCode(String roleCode){
        LambdaQueryWrapper<SystemRoleDo> eq = new LambdaQueryWrapper<SystemRoleDo>()
                .eq(SystemRoleDo::getCode, roleCode)
                .eq(SystemRoleDo::getDeleted, 0);

        return selectCount(eq) > 0;

    }

    default PageResult<SystemRoleDo> rolePage(RolePageVo rolePageVo){
        IPage<SystemRoleDo> page = new Page<>(rolePageVo.getPageNo(),rolePageVo.getPageSize());
        LambdaQueryWrapper<SystemRoleDo> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasLength(rolePageVo.getName())){
            wrapper.like(SystemRoleDo::getName,rolePageVo.getName());
        }
        if(StringUtils.hasLength(rolePageVo.getCode())){
            wrapper.like(SystemRoleDo::getCode,rolePageVo.getCode());
        }
        if(StringUtils.hasLength(rolePageVo.getStatus())){
            wrapper.eq(SystemRoleDo::getStatus,rolePageVo.getStatus());
        }
        wrapper.orderBy(true,false,SystemRoleDo::getCreateTime);
        IPage<SystemRoleDo> pag = selectPage(page, wrapper);
        return IPageToPageResultUtils.transfer(pag);

    }

    /**
     * 根据角色Code查询角色
     * @param roleCode 角色Code
     * @return 角色信息
     */
    default SystemRoleDo selectByCode(String roleCode){
        LambdaQueryWrapper<SystemRoleDo> wrapper = new LambdaQueryWrapper<SystemRoleDo>()
                .eq(SystemRoleDo::getCode, roleCode)
                .eq(SystemRoleDo::getDeleted, 0);
        return selectOne(wrapper);
    }

   default boolean replicateNameNotSelf(String name, Long id){
       LambdaQueryWrapper<SystemRoleDo> wrapper = new LambdaQueryWrapper<SystemRoleDo>()
               .eq(SystemRoleDo::getName, name)
               .eq(SystemRoleDo::getDeleted, 0)
               .ne(SystemRoleDo::getId,id);
       return selectCount(wrapper) > 0;

   }

   default boolean replicateCodeNotSelf(String code, Long id){

       LambdaQueryWrapper<SystemRoleDo> eq = new LambdaQueryWrapper<SystemRoleDo>()
               .eq(SystemRoleDo::getCode, code)
               .eq(SystemRoleDo::getDeleted, 0)
               .ne(SystemRoleDo::getId,id);

       return selectCount(eq) > 0;

   }

    List<RoleSimpleResp> getByUserGroupIds(@Param("groupIds") List<Long> userGroupIds);
}
