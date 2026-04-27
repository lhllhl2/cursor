package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.datapermission.core.scope.DataScope;
import com.jasolar.mis.framework.datapermission.core.scope.ReadWrite;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeLevel;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.datapermission.core.service.DataScopeServiceImpl;
import com.jasolar.mis.framework.mybatis.core.dataobject.IUserScopeDO;

import net.sf.jsqlparser.schema.Table;

/**
 * 人员维度的数据权限规则实现。为查询添加 user_no IN ( 'xxx' ) 的条件
 * 注意暂时系统配置页面不配置人员维度，会固定添加人员等于自己的权限。参见{@link DataScopeServiceImpl}
 * 
 * @author galuo
 * @date 2025-03-04 15:02
 *
 */
public class UserDataPermissionRule extends BaseSelfDataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.USER;
    }

    /**
     * 构建仅查询自己的数据权限范围
     * 
     * @param user
     * @return
     */
    @Override
    protected DataScope self(LoginUser user) {
        return DataScope.builder().dataIds(SetUtils.asSet(user.getNo())).type(scopeType()).rw(ReadWrite.ALL).level(ScopeLevel.SELF).build();
    }

    @Override
    protected boolean isSelf(Class<?> entityClass) {
        return false;
    }

    @Override
    protected List<DataScope> findSelfDataScopes(LoginUser user, Table table) {
        // 如果没有配置人员权限,则默认查询自己的数据
        return Arrays.asList(self(user));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的人员字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> IUserScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<IUserScopeDO>) table.getEntityType(), IUserScopeDO::getUserNo);
        });
    }

}
