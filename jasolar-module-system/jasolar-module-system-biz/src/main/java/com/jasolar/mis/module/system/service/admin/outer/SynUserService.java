package com.jasolar.mis.module.system.service.admin.outer;

import com.jasolar.mis.module.system.controller.admin.outer.resp.EsbInfoResp;
import com.jasolar.mis.module.system.controller.admin.outer.vo.synuser.SynUserParams;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 17:47
 * Version : 1.0
 */
public interface SynUserService {

    /**
     * 同步新增接口
     * @param synUserParams
     * @return
     */
    EsbInfoResp sysAddUser(SynUserParams synUserParams);

    /**
     * 更新
     * @param synUserParams
     * @return
     */
    EsbInfoResp sysUpdateUser(SynUserParams synUserParams);


    /**
     * 删除
     * @param synUserParams
     * @return
     */
    EsbInfoResp delete(SynUserParams synUserParams);
}
