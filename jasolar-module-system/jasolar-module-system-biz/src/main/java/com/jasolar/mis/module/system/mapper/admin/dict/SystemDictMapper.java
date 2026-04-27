package com.jasolar.mis.module.system.mapper.admin.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictEditVo;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictSearchParams;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemDictMapper extends BaseMapperX<SystemDictDo> {



    default boolean filedExist(SFunction<SystemDictDo, ?> field,Object value){

        LambdaQueryWrapper<SystemDictDo> eq = new LambdaQueryWrapper<SystemDictDo>()
                .eq(field, value)
                .eq(SystemDictDo::getDeleted, 0);
        return selectCount(eq) > 0;
    }


    default boolean filedExistNotSelf(SFunction<SystemDictDo, ?> field,Object value,Long id){

        LambdaQueryWrapper<SystemDictDo> eq = new LambdaQueryWrapper<SystemDictDo>()
                .eq(field, value)
                .ne(SystemDictDo::getId,id)
                .eq(SystemDictDo::getDeleted, 0);
        return selectCount(eq) > 0;
    }


    IPage<DictEditVo> selectPageBy(IPage<DictEditVo> iPage,@Param("search") DictSearchParams dictSearchParams);

    List<DictEditVo> selectDictSimpleInfoByCode(@Param("codes") List<String> codes);
}