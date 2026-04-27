package com.jasolar.mis.framework.datapermission.core.field;

import com.jasolar.mis.framework.datapermission.core.annotation.FieldPermission;

/**
 * 数据字段权限处理器
 *
 * @author zhangj
 */
public interface FieldPermissionHandler {

    /**
     * 是否字段具有权限
     *
     * @param origin 原始字符串
     * @param annotation 注解信息
     * @return true: 有权限 false: 没有权限
     */
    boolean hasPermission(Object origin, FieldPermission annotation);

}
