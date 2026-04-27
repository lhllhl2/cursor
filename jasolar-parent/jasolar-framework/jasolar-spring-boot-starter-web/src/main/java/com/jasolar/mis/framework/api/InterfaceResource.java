package com.jasolar.mis.framework.api;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhahuang
 */
@Data
@Builder
public class InterfaceResource {
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
     * 权限标识
     */
    private String permissionKey;
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
     * 状态(0-禁用, 1-启用)
     */
    private Short status;
}
