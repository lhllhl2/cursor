package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 包括法人数据权限字段的接口
 * 
 * @author galuo
 * @date 2025-03-04 14:23
 *
 */
public interface ILegalScopeDO {

    /**
     * @return 法人编码
     */
    String getLegalCode();

    /**
     * 设置法人编码
     * 
     * @param legalCode 法人编码
     */
    void setLegalCode(String legalCode);

}
