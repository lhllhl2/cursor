package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 事业群维度的权限控制
 * 
 * @author galuo
 * @date 2025-03-17 15:13
 *
 */
public interface IBusinessGroupScopeDO {
    /**
     * 事业群编码
     * 
     * @return 事业群编码
     */
    String getBusinessGroupCode();

    /**
     * 设置 事业群编码
     *
     * @param businessGroupCode 事业群编码
     */
    void setBusinessGroupCode(String businessGroupCode);
}
