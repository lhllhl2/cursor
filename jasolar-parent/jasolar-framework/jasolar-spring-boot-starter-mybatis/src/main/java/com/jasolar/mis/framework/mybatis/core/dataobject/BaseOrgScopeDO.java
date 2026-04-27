package com.jasolar.mis.framework.mybatis.core.dataobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 包括人员和部门权限字段的基类。人员和部门一般业务表中都有，一般不单独分开
 * 
 * @author galuo
 * @date 2025-03-04 14:09
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseOrgScopeDO extends BaseIdentityDO implements IUserScopeDO, IDeptScopeDO {

    /** 数据归属人员工号 */
    @Schema(description = "数据归属人员工号")
    private String userNo;

    /** 数据所属部门编号 */
    @Schema(description = "数据所属部门ID")
    private String deptCode;

}
