package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 
 * 包括部门数据权限字段的接口
 * 
 * @author galuo
 * @date 2025-03-04 14:30
 *
 */
public interface IDeptScopeDO {

    /**
     * @return 部门CODE
     */
    String getDeptCode();

    /**
     * 设置 部门CODE
     * 
     * @param deptCode 部门CODE
     */
    void setDeptCode(String deptCode);
}
