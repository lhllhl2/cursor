package com.jasolar.mis.module.system.mapper.admin.outer;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.user.OutClientInfoDo;
import com.jasolar.mis.module.system.oauth.ClientInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/11/2025 14:35
 * Version : 1.0
 */
@Mapper
public interface OutClientInfoMapper extends BaseMapperX<OutClientInfoDo> {


    ClientInfoResponse getClientInfo(@Param("clientName") String clientName);
}
