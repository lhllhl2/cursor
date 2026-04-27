package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 供应商权限范围，主要用于控制供应商只能查看自己的数据
 * 
 * @author galuo
 * @date 2025-06-24 17:36
 *
 */
public interface ISupplierScopeDO {

    /**
     * @return 供应商登录账号
     */
    String getSupplierNo();

    /**
     * 设置 供应商登录账号
     *
     * @param supplierNo 供应商登录账号
     */
    void setSupplierNo(String supplierNo);

}
