package com.jasolar.mis.module.system.mapper.admin.org;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.admin.org.SystemOrgDO;
import com.jasolar.mis.module.system.resp.SystemOrgResp;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 08/08/2025 11:04
 * Version : 1.0
 */
@Mapper
public interface SystemOrgMapper extends BaseMapperX<SystemOrgDO> {

    /**
     * 分页查询组织信息
     * @param page
     * @return
     */
    Page<SystemOrgResp> pageForMapper(Page<SystemOrgResp> page);

}
