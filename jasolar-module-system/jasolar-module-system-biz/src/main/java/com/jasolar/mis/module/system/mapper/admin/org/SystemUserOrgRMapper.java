package com.jasolar.mis.module.system.mapper.admin.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.org.SystemUserOrgRDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 11/08/2025 15:25
 * Version : 1.0
 */
@Mapper
public interface SystemUserOrgRMapper extends BaseMapperX<SystemUserOrgRDo> {


   default List<SystemUserOrgRDo> selectByUserId(Long userId){

       LambdaQueryWrapper<SystemUserOrgRDo> eq = new LambdaQueryWrapper<SystemUserOrgRDo>()
               .eq(SystemUserOrgRDo::getUserId, userId)
               .eq(SystemUserOrgRDo::getDeleted, 0);
       return selectList(eq);
   }


    default void deleteByUserId(Long userId){

        LambdaUpdateWrapper<SystemUserOrgRDo> set = new LambdaUpdateWrapper<SystemUserOrgRDo>()
                .eq(SystemUserOrgRDo::getUserId, userId)
                .eq(SystemUserOrgRDo::getDeleted, 0)
                .set(SystemUserOrgRDo::getDeleted, 1);
        update(set);

    }
}
