package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.IPublicWorkScopeDO;

/**
 * @Author: F1339926
 * @Date: 2025/6/4 17:40
 * @Description:
 */
public class PublicWorkDataPermissionRule extends BaseDataPermissionRule implements DataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.WORKING;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的事业处字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> IPublicWorkScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<IPublicWorkScopeDO>) table.getEntityType(), IPublicWorkScopeDO::getPublicWorkCode);
        });

    }
}
