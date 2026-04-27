package com.jasolar.mis.module.system.service.admin.outer;

import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synorg.OrgParams;
import jakarta.validation.Valid;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 15:47
 * Version : 1.0
 */
public interface SynOrganizationService {


    /**
     * 同步组织，新增
     * @param orgParams
     * @return
     */
    EsbInfoResp add(@Valid OrgParams orgParams);


    /**
     * 同步组织，修改
     * @param orgParams
     * @return
     */
    EsbInfoResp update(@Valid OrgParams orgParams);



    /**
     * 同步组织，修改
     * @param orgParams
     * @return
     */
    EsbInfoResp deleted(@Valid OrgParams orgParams);



}
