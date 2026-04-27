package com.jasolar.mis.module.system.mapper.budget;

import com.jasolar.mis.framework.mybatis.core.mapper.BaseMapperX;
import com.jasolar.mis.module.system.domain.budget.BudgetApiRequestLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 预算接口请求报文记录 Mapper
 */
@Mapper
public interface BudgetApiRequestLogMapper extends BaseMapperX<BudgetApiRequestLog> {

    /**
     * 获取序列的下一个值
     *
     * @return 序列的下一个值
     */
    @Select("SELECT SEQ_BUDGET_API_REQUEST_LOG.NEXTVAL FROM DUAL")
    Long selectNextSequenceValue();
}

