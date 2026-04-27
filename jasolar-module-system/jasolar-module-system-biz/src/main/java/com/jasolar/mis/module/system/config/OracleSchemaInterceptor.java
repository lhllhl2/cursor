package com.jasolar.mis.module.system.config;

import com.jasolar.mis.module.system.controller.budget.oa.OaCallbackSqlTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.plugin.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Oracle Schema 拦截器
 * 在执行 SQL 前自动设置当前会话的 schema
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class OracleSchemaInterceptor implements Interceptor {

    @Resource
    private Environment environment;

    // 使用 WeakHashMap 存储已设置 schema 的连接，避免重复设置
    private static final java.util.Set<Connection> SCHEMA_SET_CONNECTIONS = java.util.Collections.synchronizedSet(
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>()));

    /**
     * 拦截器初始化时打印日志
     */
    @PostConstruct
    public void init() {
        try {
            String schema = getSchema();
            log.info("========== OracleSchemaInterceptor 已创建，Schema: {} ==========", schema);
        } catch (Exception e) {
            log.error("========== OracleSchemaInterceptor 初始化失败: {} ==========", e.getMessage(), e);
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        if (OaCallbackSqlTraceContext.isEnabled()) {
            try {
                MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
                StatementHandler realStatementHandler = (StatementHandler) metaObject.getValue("h.target");
                BoundSql boundSql = realStatementHandler.getBoundSql();
                if (boundSql != null) {
                    OaCallbackSqlTraceContext.addSql(boundSql.getSql());
                }
            } catch (Exception ignored) {
                // SQL采集失败不影响主流程
            }
        }

        // StatementHandler.prepare 方法的第一个参数就是 Connection
        Connection connection = (Connection) invocation.getArgs()[0];
        
        // 设置 schema
        if (connection != null) {
            setSchemaIfNeeded(connection);
        }
        
        // 执行原始方法
        return invocation.proceed();
    }
    
    /**
     * 获取要设置的 schema（供外部调用）
     */
    public String getSchemaForLogging() {
        return getSchema();
    }

    /**
     * 设置 schema（如果需要）
     */
    private void setSchemaIfNeeded(Connection connection) {
        try {
            // 检查该连接是否已设置过 schema
            if (SCHEMA_SET_CONNECTIONS.contains(connection)) {
                return;
            }

            // 获取要设置的 schema
            String schema = getSchema();
            if (schema == null || schema.trim().isEmpty()) {
                log.warn("Schema is null or empty, cannot set schema");
                return;
            }

            // 先查询当前的 schema
            String currentSchema = null;
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL");
                if (rs.next()) {
                    currentSchema = rs.getString(1);
                }
            } finally {
                // 确保 ResultSet 和 Statement 正确关闭
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
            }

            // 如果 schema 已经正确，跳过设置
            if (currentSchema != null && schema.equalsIgnoreCase(currentSchema)) {
                SCHEMA_SET_CONNECTIONS.add(connection);
                return;
            }

            // 执行 ALTER SESSION SET CURRENT_SCHEMA
            String sql = "ALTER SESSION SET CURRENT_SCHEMA = " + schema.toUpperCase();
            java.sql.Statement alterStmt = null;
            try {
                alterStmt = connection.createStatement();
                alterStmt.execute(sql);
                log.info("========== Oracle Schema 已设置为: {} (原: {}) ==========", schema.toUpperCase(), currentSchema);
                
                // 标记该连接已设置 schema
                SCHEMA_SET_CONNECTIONS.add(connection);
            } finally {
                // 确保 Statement 正确关闭
                if (alterStmt != null) {
                    try {
                        alterStmt.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to set Oracle schema: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取要设置的 schema
     * 优先从环境变量 DB_SCHEMA 读取，否则从配置文件解析，最后使用默认值 jasolar_budget
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

    @Override
    public Object plugin(Object target) {
        // 不打印日志，减少日志输出
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过 properties 设置参数
    }
}


