package com.jasolar.mis.framework.tenant.core.db;

import java.util.Set;
import java.util.TreeSet;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import com.jasolar.mis.framework.tenant.config.TenantProperties;
import com.jasolar.mis.framework.tenant.core.context.TenantContextHolder;

import cn.hutool.core.collection.CollUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 *
 * @author zhaohuang
 */
public class TenantDatabaseInterceptor implements TenantLineHandler {

    /** 需略的表名,不区分大小写 */
    private final Set<String> ignoreTables = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public TenantDatabaseInterceptor(TenantProperties properties) {
        // // 不同 DB 下，大小写的习惯不同，所以需要都添加进去
        // properties.getIgnoreTables().forEach(table -> {
        // ignoreTables.add(table.toLowerCase());
        // ignoreTables.add(table.toUpperCase());
        // });
        // 在 OracleKeyGenerator 中，生成主键时，会查询这个表，查询这个表后，会自动拼接 TENANT_ID 导致报错
        ignoreTables.add("DUAL");
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TenantContextHolder.isIgnore() // 情况一，全局忽略多租户
                || CollUtil.contains(ignoreTables, SqlParserUtils.removeWrapperSymbol(tableName)); // 情况二，忽略多租户的表
    }

}
