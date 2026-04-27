package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.IBusinessUnitScopeDO;

/**
 * 事业处的数据权限维度
 * 
 * @author galuo
 * @date 2025-03-17 15:44
 *
 */
public class BusinessUnitDataPermissionRule extends BaseDataPermissionRule implements DataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.BUSINESS_UNIT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的事业处字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> IBusinessUnitScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<IBusinessUnitScopeDO>) table.getEntityType(), IBusinessUnitScopeDO::getBusinessUnitCode);
        });

    }
}
