package com.jasolar.mis.framework.data.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.Supplier;
import com.jasolar.mis.module.supplier.api.admin.dto.SupplierRespDTO;

/**
 * 
 * 人员信息的数据转换
 * 
 * @author xinzhou
 * @date 2025-04-03 14:18:30
 *
 */
@Mapper
public interface SupplierConverter {

    /** 默认的实例 */
    SupplierConverter INSTANCE = Mappers.getMapper(SupplierConverter.class);

    /**
     * 将查询对象转换为缓存对象
     * 
     * @param dto 查询到的DTO
     * @return 缓存对象
     */
    Supplier convert(SupplierRespDTO dto);

}
