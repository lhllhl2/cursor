package com.jasolar.mis.framework.bpm.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.mybatis.core.dataobject.IBpmBizDO;
import com.jasolar.mis.module.bpm.api.dto.BpmProcessInstanceCreateReqDTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BpmProcessConverter {

    BpmProcessConverter INSTANCE = Mappers.getMapper(BpmProcessConverter.class);

    /**
     * 转换为创建流程的请求DTO
     * 
     * @param entity 业务实体
     * @return 创建流程的请求DTO
     */
    @Mapping(source = "id", target = "bizId")
    @Mapping(source = "no", target = "bizNo")
    BpmProcessInstanceCreateReqDTO toRequestDTO(IBpmBizDO entity);

}
