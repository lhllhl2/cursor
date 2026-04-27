package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.ILegalScopeDO;

/**
 * 法人维度的数据权限规则实现。为查询添加 legal_code IN ( 'xxx' ) 的条件
 * 
 * @author galuo
 * @date 2025-03-04 15:38
 *
 */
public class LegalDataPermissionRule extends BaseDataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.LEGAL;
    }

    // /**
    // * 构建仅查询自己的数据权限范围
    // *
    // * @param user
    // * @return
    // */
    // protected DataScope self(LoginUser user) {
    // User usr = UserUtils.get(user.getNo());
    // return DataScope.builder().dataIds(SetUtils.asSet(usr.getLegalCode())).type(scopeType()).rw(ReadWrite.ALL).level(ScopeLevel.SELF)
    // .build();
    // }
    //
    // @Override
    // protected boolean isSelf(Class<?> entityClass) {
    // return ILegalDO.class.isAssignableFrom(entityClass);
    // }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的人员字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> ILegalScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<ILegalScopeDO>) table.getEntityType(), ILegalScopeDO::getLegalCode);
        });
    }

}
