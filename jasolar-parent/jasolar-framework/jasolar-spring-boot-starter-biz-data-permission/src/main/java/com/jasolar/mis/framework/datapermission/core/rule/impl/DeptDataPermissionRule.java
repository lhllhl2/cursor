package com.jasolar.mis.framework.datapermission.core.rule.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jasolar.mis.framework.datapermission.core.rule.BaseDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.scope.ScopeType;
import com.jasolar.mis.framework.mybatis.core.dataobject.IDeptScopeDO;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 部门维度的数据权限规则实现。为查询添加 dept_code IN ( 'xxx' ) 的条件
 * 
 * @author galuo
 * @date 2025-03-04 15:36
 *
 */
@Slf4j
public class DeptDataPermissionRule extends BaseDataPermissionRule {

    @Override
    public ScopeType scopeType() {
        return ScopeType.DEPT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化所有业务表的人员字段
        List<TableInfo> tables = TableInfoHelper.getTableInfos();
        tables.stream().filter(t -> IDeptScopeDO.class.isAssignableFrom(t.getEntityType())).forEach(table -> {
            this.addDefaultTable((Class<IDeptScopeDO>) table.getEntityType(), IDeptScopeDO::getDeptCode);
        });
    }

    // @Override
    // protected boolean isSelf(Class<?> entityClass) {
    // return IDeptDO.class.isAssignableFrom(entityClass);
    // }
    //
    // @Override
    // protected DataScope self(LoginUser user) {
    // User usr = UserUtils.get(user.getNo());
    // return DataScope.builder().dataIds(SetUtils.asSet(usr.getDeptCode())).type(scopeType()).rw(ReadWrite.ALL).level(ScopeLevel.SELF)
    // .build();
    // }

}
