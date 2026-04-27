package com.jasolar.mis.module.system.mapper.admin.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.dict.vo.DictLabelVo;
import com.jasolar.mis.module.system.domain.admin.dict.SystemDictLabelDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemDictLabelMapper extends BaseMapperX<SystemDictLabelDo> {



    default List<SystemDictLabelDo> selectBySimpleFields(SFunction<SystemDictLabelDo, ?> field, Object value){

        LambdaQueryWrapper<SystemDictLabelDo> eq = new LambdaQueryWrapper<SystemDictLabelDo>()
                .eq(field, value)
                .eq(SystemDictLabelDo::getDeleted, 0);
        return selectList(eq);

    }

    List<DictLabelVo> searchByDictIds(@Param("dictIds") List<Long> dictIds);

    default void deleteByDictId( Long dictId){
        LambdaUpdateWrapper<SystemDictLabelDo> eq = new LambdaUpdateWrapper<SystemDictLabelDo>()
                .set(SystemDictLabelDo::getDeleted,1)
                .eq(SystemDictLabelDo::getDictId, dictId)
                .eq(SystemDictLabelDo::getDeleted, 0);
        update(eq);
    }


}