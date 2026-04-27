package com.jasolar.mis.module.system.config;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.sql.Statement;
import java.util.Properties;

/**
 * Druid 过滤器
 * 在每次获取连接时设置 Oracle schema
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Slf4j
@Component
public class OracleSchemaFilter extends FilterEventAdapter {

    @Resource
    private Environment environment;

    private String schema;

    @PostConstruct
    public void init() {
        schema = getSchema();
        log.info("========== OracleSchemaFilter 已创建，Schema: {} ==========", schema);
    }

    @Override
    public void connection_connectBefore(FilterChain chain, Properties info) {
        super.connection_connectBefore(chain, info);
    }

    // 使用 ThreadLocal 存储每个连接是否已设置 schema
    private static final java.util.Set<ConnectionProxy> SCHEMA_SET_CONNECTIONS = 
        java.util.Collections.synchronizedSet(new java.util.WeakHashMap<ConnectionProxy, Boolean>().keySet());

    @Override
    public void connection_connectAfter(ConnectionProxy connection) {
        super.connection_connectAfter(connection);
        setSchemaIfNeeded(connection);
    }

    /**
     * 设置 schema（如果需要）
     */
    private void setSchemaIfNeeded(ConnectionProxy connection) {
        try {
            // 检查该连接是否已设置过 schema
            if (SCHEMA_SET_CONNECTIONS.contains(connection)) {
                return;
            }

            String schemaToSet = getSchema();
            if (schemaToSet == null || schemaToSet.trim().isEmpty()) {
                log.warn("Schema is null or empty, cannot set schema");
                return;
            }

            // 先查询当前的 schema
            try (Statement stmt = connection.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL")) {
                if (rs.next()) {
                    String currentSchema = rs.getString(1);
                    if (!schemaToSet.equalsIgnoreCase(currentSchema)) {
                        // 执行 ALTER SESSION SET CURRENT_SCHEMA
                        String sql = "ALTER SESSION SET CURRENT_SCHEMA = " + schemaToSet.toUpperCase();
                        try (Statement alterStmt = connection.createStatement()) {
                            alterStmt.execute(sql);
                            log.info("========== Druid Filter: Oracle Schema 已设置为: {} (原: {}) ==========", 
                                    schemaToSet.toUpperCase(), currentSchema);
                            // 标记该连接已设置 schema
                            SCHEMA_SET_CONNECTIONS.add(connection);
                        }
                    } else {
                        log.debug("========== Druid Filter: Schema 已经是 {}, 跳过设置 ==========", currentSchema);
                        // 标记该连接已设置 schema
                        SCHEMA_SET_CONNECTIONS.add(connection);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to set Oracle schema in Druid filter: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取要设置的 schema
     */
    private String getSchema() {
        // 1. 优先从环境变量读取
        String schema = environment.getProperty("DB_SCHEMA");
        if (schema != null && !schema.trim().isEmpty()) {
            return schema.trim();
        }
        
        // 2. 从配置文件中读取 connection-init-sql 并解析
        String connectionInitSql = environment.getProperty("spring.datasource.dynamic.datasource.master.connection-init-sql");
        if (connectionInitSql != null && connectionInitSql.contains("CURRENT_SCHEMA")) {
            // 解析 ALTER SESSION SET CURRENT_SCHEMA = ${DB_SCHEMA:jasolar_budget}
            int equalsIndex = connectionInitSql.indexOf('=');
            if (equalsIndex > 0) {
                schema = connectionInitSql.substring(equalsIndex + 1).trim();
                // 移除可能的引号
                schema = schema.replaceAll("^['\"]|['\"]$", "");
                // 解析 ${DB_SCHEMA:jasolar_budget} 格式
                if (schema.startsWith("${") && schema.endsWith("}")) {
                    // 提取默认值部分
                    int colonIndex = schema.indexOf(':');
                    if (colonIndex > 0) {
                        schema = schema.substring(colonIndex + 1, schema.length() - 1);
                    } else {
                        // 没有默认值，尝试从环境变量读取
                        String envVar = schema.substring(2, schema.length() - 1);
                        schema = environment.getProperty(envVar, "jasolar_budget");
                    }
                }
                if (schema != null && !schema.trim().isEmpty()) {
                    return schema.trim();
                }
            }
        }
        
        // 3. 使用默认值
        return "jasolar_budget";
    }
}

