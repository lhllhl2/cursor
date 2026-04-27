package com.jasolar.mis.module.system.api.permission.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人员的数据权限范围
 * 
 * @author galuo
 * @date 2025-03-05 16:50
 *
 */
@Data
@Schema(description = "RPC 服务 - 查询人员的数据权限时返回的数据")
public class DataPermissionDTO {

    /** 菜单ID */
    private Long menuId;

    /** 是否可查询空值的数据, 如采购员在请购池中可查询尚未分配采购员的数据 */
    private Boolean nullable;

    /**
     * 权限类型
     * 
     * @see com.fiifoxconn.mis.framework.datapermission.core.scope.ScopeType
     */
    @NotNull
    private Integer scopeType;

    /**
     * 读写分离权限
     * 
     * @see com.fiifoxconn.mis.framework.datapermission.core.scope.ReadWrite
     */
    @NotNull
    private Integer readWrite;

    /**
     * 数据权限等级，此字段除了全部权限之外，均会计算出{@link #dataIds}
     * 
     * @see com.fiifoxconn.mis.framework.datapermission.core.scope.ScopeLevel
     */
    private Integer scopeLevel;

    /** 查询的数据ID */
    @NotEmpty
    private Set<String> dataIds;

}
