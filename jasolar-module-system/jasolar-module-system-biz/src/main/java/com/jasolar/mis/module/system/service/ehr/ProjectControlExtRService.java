package com.jasolar.mis.module.system.service.ehr;

import java.util.List;

/**
 * 项目控制扩展关系 Service 接口
 */
public interface ProjectControlExtRService {

    /**
     * 同步项目控制关系数据
     * @param prjCds 项目编码列表
     * @return 处理结果描述
     */
    String syncProjectControlRData(List<String> prjCds);

}

