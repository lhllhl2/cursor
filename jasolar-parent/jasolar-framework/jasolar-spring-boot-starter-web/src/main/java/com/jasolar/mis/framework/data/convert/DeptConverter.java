package com.jasolar.mis.framework.data.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.Dept;
// import com.jasolar.mis.module.system.api.dept.dto.DeptRespDTO;

/**
 * 
 * 部门信息的数据转换
 * 
 * @author galuo
 * @date 2025-03-28 16:15
 *
 */
@Mapper
public interface DeptConverter {

    /** 默认的实例 */
    DeptConverter INSTANCE = Mappers.getMapper(DeptConverter.class);

    /**
     * 数据转换
     * 
     * @param dto DTO
     * @return Dept对象
     */
    // @Mapping(source = "code", target = "deptCode")
    // @Mapping(source = "name", target = "deptName")
    // Dept convert(DeptRespDTO dto);

    /**
     * 转换列表数据
     * 
     * @param list DTO列表
     * @return Dept列表
     */
    // List<Dept> convert(List<DeptRespDTO> list);

}
