package com.jasolar.mis.module.system.service.admin.log;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.module.system.controller.admin.log.resp.LogResp;
import com.jasolar.mis.module.system.controller.admin.log.vo.LogPageVo;
import com.jasolar.mis.module.system.mapper.admin.log.SystemLogMapper;
import com.jasolar.mis.module.system.util.IPageToPageResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 27/08/2025 15:09
 * Version : 1.0
 */
@Service
public class LogServiceImpl implements LogService{


    @Autowired
    private SystemLogMapper systemLogMapper;

    @Override
    public PageResult<LogResp> logPage(LogPageVo logPageVo) {
        IPage<LogResp> page = new Page<>(logPageVo.getPageNo(),logPageVo.getPageSize());
        page = systemLogMapper.logPage(page,logPageVo);
        return IPageToPageResultUtils.transfer(page);
    }
}
