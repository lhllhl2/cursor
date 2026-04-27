package com.jasolar.mis.framework.data.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.data.core.Legal;
import com.jasolar.mis.module.system.api.legal.dto.LegalDTO;

/**
 * 
 * 法人信息的数据转换
 * 
 * @author galuo
 * @date 2025-03-28 16:16
 *
 */
@Mapper
public interface LegalConverter {

    /** 默认的实例 */
    LegalConverter INSTANCE = Mappers.getMapper(LegalConverter.class);

    /**
     * 数据转换
     * 
     * @param dto DTO
     * @return Legal对象
     */
    @Mapping(source = "code", target = "legalCode")
    @Mapping(source = "name", target = "legalName")
    Legal convert(LegalDTO dto);

    /**
     * 转换列表数据
     * 
     * @param list DTO列表
     * @return Legal列表
     */
    List<Legal> convert(List<LegalDTO> list);
}
