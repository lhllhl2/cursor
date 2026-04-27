package com.jasolar.mis.framework.data.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.DictData;
import com.jasolar.mis.module.system.api.dict.dto.DictDataRespDTO;

/**
 * 字典数据转换
 * 
 * @author galuo
 * @date 2025-04-01 17:11
 *
 */
@Mapper
public interface DictConverter {

    /** 默认的实例 */
    DictConverter INSTANCE = Mappers.getMapper(DictConverter.class);

    /**
     * 数据转换
     * 
     * @param dto DTO
     * @return DictData对象
     */
    DictData convert(DictDataRespDTO dto);

    /**
     * 转换列表数据
     * 
     * @param list DTO列表
     * @return DictData列表
     */
    List<DictData> convert(List<DictDataRespDTO> list);
}
