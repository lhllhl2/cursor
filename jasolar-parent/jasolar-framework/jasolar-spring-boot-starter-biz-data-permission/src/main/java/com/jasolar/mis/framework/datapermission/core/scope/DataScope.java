package com.jasolar.mis.framework.datapermission.core.scope;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限范围，通过角色的配置读取进行初始化
 * 
 * @author galuo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScope {

    /** 菜单ID */
    private Long menuId;

    /** 是否可查询空值的数据, 如采购员在请购池中可查询尚未分配采购员的数据 */
    private boolean nullable;

    /** 权限类型。 一个权限类型就有一个实现的规则 */
    private ScopeType type;

    /** 权限等级, 不同的权限类型有不同的权限等级 */
    private ScopeLevel level;

    /** 指定的数据id列表. 一般当{@link #level}为{@link ScopeLevel#ID}时有效 */
    private Set<String> dataIds;

    /** 读写分离,适用于读或写接口 */
    private ReadWrite rw;

}
