package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeLevel;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.ISupplierDO;
import com.jasolar.mis.framework.mybatis.core.dataobject.ISupplierScopeDO;

import net.sf.jsqlparser.schema.Table;

/**
 * 供应商端添加权限
 * 
 * @author galuo
 * @date 2025-06-24 17:53
 *
 */
public class SupplierPortalDataPermissionRule extends BaseDataPermissionRule {

    @Override
    public boolean accept(UserTypeEnum userType) {
        return UserTypeEnum.SUPPLIER == userType;
    }

    @Override
    protected List<DataScope> findDataScopes(LoginUser user, Table table) {
        // if (user == null || user.userType() != UserTypeEnum.SUPPLIER) {
        // // 非供应商没有权限控制
        // return Collections.emptyList();
        // }

        DataScope self = DataScope.builder().dataIds(SetUtils.asSet(user.getNo())).type(scopeType()).rw(ReadWrite.ALL)
                .level(ScopeLevel.SELF).build();
        return Arrays.asList(self);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的人员字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> ISupplierScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<ISupplierScopeDO>) table.getEntityType(), ISupplierScopeDO::getSupplierNo);
        });

        tables.stream().filter(t -> ISupplierDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<ISupplierDO>) table.getEntityType(), ISupplierDO::getNo);
        });
    }

    @Override
    public ScopeType scopeType() {
        return ScopeType.SUPPLIER_PORTAL;
    }

}
