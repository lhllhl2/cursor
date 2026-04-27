package com.jasolar.mis.framework.datapermission.core.aop;

import java.util.Set;

import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;
import com.jasolar.mis.framework.datapermission.core.annotation.EnableDataPermission;

import lombok.Data;

/**
 * 权限注解容器,用于线程中注入注解参数
 * 
 * @author galuo
 * @date 2025-03-04 17:16
 *
 */
@Data
public class DataPermissionContext {

    /** 是否启用数据权限, 通过{@link EnableDataPermission}注解控制. 为null则表示仅配置了权限规则 */
    private Boolean enabled;

    /** 数据权限规则,通过{@link DataPermission}注解注入 */
    private Set<DataPermission> permissions;

    /**
     * 是否包含任意规则
     * 
     * @return true表示{@link #permissions}数组中有至少一条数据
     */
    public boolean hasRule() {
        return permissions != null && !permissions.isEmpty();
    }

    /**
     * 禁用数据权限的对象
     * 
     * @return
     */
    public static DataPermissionContext disable() {
        DataPermissionContext c = new DataPermissionContext();
        c.enabled = false;
        return c;
    }

    /**
     * 启用数据权限的对象
     * 
     * @return
     */
    public static DataPermissionContext enable() {
        DataPermissionContext c = new DataPermissionContext();
        c.enabled = true;
        return c;
    }

    /**
     * 通过使用的数据权限规则构造
     * 
     * @param permissions 注解配置的权限规则
     * @return
     */
    public static DataPermissionContext of(Set<DataPermission> permissions) {
        DataPermissionContext c = new DataPermissionContext();
        c.permissions = permissions;
        return c;
    }

}
