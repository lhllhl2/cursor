package com.jasolar.mis.module.system.controller.admin.outer;

import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.resp.SynResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.SynUserParams;
import com.jasolar.mis.module.system.service.admin.outer.SynUserService;
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
 * Date : 06/08/2025 16:01
 * Version : 1.0
 */
@RestController
@RequestMapping(value = "/user")
@Slf4j
public class SynUserController {


    @Autowired
    private SynUserService synUserService;


    @PostMapping("/add")
    public SynResp sysAddUser(@RequestBody @Valid SynUserParams synUserParams){

        log.info("synUserParams add --> {}",synUserParams);

        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synUserService.sysAddUser(synUserParams);
        }catch (Exception e){
            log.info("新增失败",e);
            return SynResp.failed("用户新增失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }


    @PostMapping("/update")
    public SynResp sysUpdate(@RequestBody @Valid SynUserParams synUserParams){
        log.info("synUserParams update --> {}",synUserParams);
        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synUserService.sysUpdateUser(synUserParams);
        }catch (Exception e){
            log.error("user update failed ...",e);
            return SynResp.failed("用户新增失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }


    @PostMapping("/delete")
    public SynResp sysUserDel(@RequestBody @Valid SynUserParams synUserParams ){
        log.info("synUserParams delete --> {}",synUserParams);
        EsbInfoResp esbInfoResp = null;
        try {
            esbInfoResp = synUserService.delete(synUserParams);
        }catch (Exception e){
            log.error("user delete failed ...",e);
            return SynResp.failed("用户删除失败",esbInfoResp);
        }
        return  SynResp.success(esbInfoResp.getReturnMsg(),esbInfoResp);
    }




}
