package com.jasolar.mis.module.system.mapper.admin.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.controller.admin.log.resp.LogResp;
import com.jasolar.mis.module.system.controller.admin.log.vo.LogPageVo;
import com.jasolar.mis.module.system.domain.admin.log.SystemLogDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 14:52
 * Version : 1.0
 */
@Mapper
public interface SystemLogMapper extends BaseMapperX<SystemLogDo>  {


    IPage<LogResp> logPage(IPage<LogResp> page, @Param("search") LogPageVo logPageVo);
}
