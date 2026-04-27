package com.jasolar.mis.module.system.domain.admin.permission;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseIdentityDO;
import lombok.*;

/**
 * 接口资源 DO
 *
 * @author zhahuang
 */
@TableName("system_interface_resource")
@Data
@EqualsAndHashCode(callSuper = true, of = {
        "serviceName", "categoryName", "controllerName", "functionName", "name", "url", "method", "description", "permissionKey", "status"
})

@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceResourceDO extends BaseIdentityDO {

    /**
     * 微服务名称
     */
    private String serviceName;
    /**
     * 接口分类名称
     */
    private String categoryName;
    /**
     * 接口所属控制器名称
     */
    private String controllerName;
    /**
     * 接口所在方法名称
     */
    private String functionName;
    /**
     * 接口名称
     */
    private String name;
    /**
     * 接口URL
     */
    private String url;
    /**
     * HTTP方法(GET,POST,PUT,DELETE等)
     */
    private String method;
    /**
     * 接口描述
     */
    private String description;

    /**
     * 权限标识
     */
    private String permissionKey;


    /**
     * 状态(0-禁用, 1-启用)
     */
    private Short status;

}