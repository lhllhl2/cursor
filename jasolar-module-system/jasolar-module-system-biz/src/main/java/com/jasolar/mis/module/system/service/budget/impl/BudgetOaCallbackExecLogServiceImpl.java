package com.jasolar.mis.module.system.service.budget.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jasolar.mis.module.system.domain.budget.BudgetOaCallbackExecLog;
import com.jasolar.mis.module.system.mapper.budget.BudgetOaCallbackExecLogMapper;
import com.jasolar.mis.module.system.service.budget.BudgetOaCallbackExecLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * OA回调执行日志服务实现
 */
@Slf4j
@Service
public class BudgetOaCallbackExecLogServiceImpl implements BudgetOaCallbackExecLogService {

    @Resource
    private BudgetOaCallbackExecLogMapper budgetOaCallbackExecLogMapper;

    @Override
    @Async
    public void record(BudgetOaCallbackExecLog execLog) {
        try {
            if (execLog.getId() == null) {
                execLog.setId(IdWorker.getId());
            }
            budgetOaCallbackExecLogMapper.insert(execLog);
        } catch (Exception ex) {
            log.error("记录OA回调执行日志失败", ex);
        }
    }
}
