package com.jasolar.mis.framework.data.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.Material;
import com.jasolar.mis.module.masterdata.dto.MaterialDTO;

/**
 * 物料主数据
 * 
 * @author galuo
 * @date 2025-04-09 20:55
 *
 */
@Mapper
public interface MaterialConverter {
    /** 默认的实例 */
    MaterialConverter INSTANCE = Mappers.getMapper(MaterialConverter.class);

    /**
     * 将查询对象转换为缓存对象
     * 
     * @param dto 查询到的DTO
     * @return 缓存对象
     */
    Material convert(MaterialDTO dto);

}
