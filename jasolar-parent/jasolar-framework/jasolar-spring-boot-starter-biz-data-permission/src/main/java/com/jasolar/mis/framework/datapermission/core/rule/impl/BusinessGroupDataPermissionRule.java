package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.IBusinessGroupScopeDO;

/**
 * 事业群的权限维度
 * 
 * @author galuo
 * @date 2025-03-17 15:45
 *
 */
public class BusinessGroupDataPermissionRule extends BaseDataPermissionRule implements DataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.BUSINESS_GROUP;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的事业群字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> IBusinessGroupScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<IBusinessGroupScopeDO>) table.getEntityType(), IBusinessGroupScopeDO::getBusinessGroupCode);
        });

    }

}
