package com.jasolar.mis.module.system.controller.admin.outer;

import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.resp.SynResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synorg.OrgParams;
import com.jasolar.mis.module.system.service.admin.outer.SynOrganizationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 07/08/2025 15:27
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/organization")
@Slf4j
public class SynOrganizationController {


    @Autowired
    private SynOrganizationService synOrganizationService;

    @PostMapping("/add")
    public SynResp addOrg(@RequestBody @Valid OrgParams orgParams){
        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synOrganizationService.add(orgParams);
        }catch (Exception e){
            log.error("org add failed ...",e);
            return SynResp.failed("组织新增失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }



    @PostMapping("/update")
    public SynResp updateOrg(@RequestBody @Valid OrgParams orgParams){
        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synOrganizationService.update(orgParams);
        }catch (Exception e){
            log.error("org update failed ...",e);
            return SynResp.failed("组织修改失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }




    @PostMapping("/delete")
    public SynResp deleteOrg(@RequestBody @Valid OrgParams orgParams){
        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synOrganizationService.deleted(orgParams);
        }catch (Exception e){
            log.error("org deleted failed ...",e);
            return SynResp.failed("组织删除失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }





}
