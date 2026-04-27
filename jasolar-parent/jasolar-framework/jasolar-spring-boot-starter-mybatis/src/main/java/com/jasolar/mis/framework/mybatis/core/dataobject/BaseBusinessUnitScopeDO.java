package com.jasolar.mis.framework.mybatis.core.dataobject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 事业处编号字段
 * 
 * @author galuo
 * @date 2025-03-17 15:16
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseBusinessUnitScopeDO extends BaseIdentityDO implements IBusinessUnitScopeDO {

    /** 事业处编号 */
    private String businessUnitCode;
}
