package com.jasolar.mis.module.system.service.budget.impl;

import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLog;
import com.jasolar.mis.module.system.mapper.budget.BudgetApiRequestLogMapper;
import com.jasolar.mis.module.system.service.budget.BudgetApiRequestLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 预算接口请求报文记录服务实现类
 */
@Slf4j
@Service
public class BudgetApiRequestLogServiceImpl implements BudgetApiRequestLogService {

    @Resource
    private BudgetApiRequestLogMapper budgetApiRequestLogMapper;

    /**
     * 记录接口请求报文（异步执行，避免影响主流程）
     *
     * @param apiRequestLog 请求日志实体
     */
    @Override
    @Async
    public void recordRequestLog(BudgetApiRequestLog apiRequestLog) {
        try {
            // 由于使用了触发器自动赋值ID，这里直接插入即可，ID留空让触发器自动赋值
            // 但MyBatis Plus的insert方法可能需要ID，所以我们先获取序列值
            Long nextId = budgetApiRequestLogMapper.selectNextSequenceValue();
            apiRequestLog.setId(nextId);
            budgetApiRequestLogMapper.insert(apiRequestLog);
        } catch (Exception e) {
            // 记录日志失败不应该影响主流程，只记录错误日志
            log.error("记录接口请求报文失败", e);
        }
    }
}

