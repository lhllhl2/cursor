package com.jasolar.mis.framework.biztask.mq;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.jasolar.mis.framework.biztask.BizTask;
import com.jasolar.mis.framework.biztask.BizTaskKey;

/**
 * 业务待办消息对象的转换
 * 
 * @author galuo
 * @date 2025-04-14 17:07
 *
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BizTaskMessageConverter {

    BizTaskMessageConverter INSTANCE = Mappers.getMapper(BizTaskMessageConverter.class);

    /**
     * 构建任务对象
     * 
     * @param key 业务主键
     * @param title 标题
     * @param content 内容
     * @return
     */
    BizTask convert2Task(BizTaskKey key, String title, String content);

    @Mapping(target = "userType", source = "userType.value")
    @Mapping(target = "module", source = "module.name")
    BizTaskMessage convert(BizTaskKey key);

    @Mapping(target = "userType", source = "userType.value")
    @Mapping(target = "module", source = "module.name")
    BizTaskMessage convert(BizTask task);

}
