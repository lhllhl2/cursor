package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 
 * 事业处维度的权限控制
 * 
 * @author galuo
 * @date 2025-03-17 15:14
 *
 */
public interface IBusinessUnitScopeDO {

    /**
     * 事业处编码
     * 
     * @return 事业处编码
     */
    String getBusinessUnitCode();

    /**
     * 设置 事业处编码
     *
     * @param businessUnitCode 事业处编码
     */
    void setBusinessUnitCode(String businessUnitCode);
}
