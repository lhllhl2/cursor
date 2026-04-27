package com.jasolar.mis.framework.mybatis.core.dataobject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 仅包含法人实体的数据权限
 * 
 * @author galuo
 * @date 2025-03-04 12:11
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseLegalScopeDO extends BaseIdentityDO implements ILegalScopeDO {

    /** 数据所属法人编号 */
    private String legalCode;

}
