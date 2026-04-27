package com.jasolar.mis.framework.data.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.MaterialCategory;
import com.jasolar.mis.module.masterdata.dto.MaterialCategoryDTO;

/**
 * 物料分类的转换
 * 
 * @author galuo
 * @date 2025-04-09 12:13
 *
 */
@Mapper
public interface MaterialCategoryConverter {

    /** 默认的实例 */
    MaterialCategoryConverter INSTANCE = Mappers.getMapper(MaterialCategoryConverter.class);

    /**
     * 将查询对象转换为缓存对象
     * 
     * @param dto 查询到的DTO
     * @return 缓存对象
     */
    MaterialCategory convert(MaterialCategoryDTO dto);

}
