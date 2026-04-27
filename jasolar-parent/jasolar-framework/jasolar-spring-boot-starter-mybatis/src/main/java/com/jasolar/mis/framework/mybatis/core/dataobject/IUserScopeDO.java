package com.jasolar.mis.framework.mybatis.core.dataobject;

/**
 * 包括人员维度数据权限字段的接口
 * 
 * @author galuo
 * @date 2025-03-04 14:28
 *
 */
public interface IUserScopeDO {
    /**
     * @return 人员工号
     */
    String getUserNo();

    /**
     * 设置 人员工号
     *
     * @param userNo 人员工号
     */
    void setUserNo(String userNo);
}
