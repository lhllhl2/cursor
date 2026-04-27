package com.jasolar.mis.framework.mybatis.config;

import java.util.concurrent.TimeUnit;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.incrementer.DmKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.KingbaseKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.PostgreKeyGenerator;
import com.baomidou.mybatisplus.extension.parser.JsqlParserGlobal;
import com.baomidou.mybatisplus.extension.parser.cache.JdkSerialCaffeineJsqlParseCache;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.jasolar.mis.framework.mybatis.core.handler.DefaultDBFieldHandler;
import com.jasolar.mis.framework.mybatis.core.id.RedisWorker;
import com.jasolar.mis.framework.mybatis.core.id.WorkerOnlySnowflakeIdGenerator;
import com.jasolar.mis.framework.mybatis.core.type.OracleLocalDateTimeTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.text.CharSequenceUtil;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * MyBaits 配置类
 *
 * @author zhaohuang
 */
@Slf4j
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class)
// 目的：先于 MyBatis Plus 自动配置，避免 @MapperScan 可能扫描不到 Mapper 打印 warn 日志
@MapperScan(value = "${jasolar.info.base-package}", annotationClass = Mapper.class,
        lazyInitialization = "${mybatis.lazy-initialization:false}") // Mapper 懒加载，目前仅用于单元测试
public class JasolarMybatisAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    static {
        // 动态 SQL 智能优化支持本地缓存加速解析，更完善的租户复杂 XML 动态 SQL 支持，静态注入缓存
        JsqlParserGlobal.setJsqlParseCache(
                new JdkSerialCaffeineJsqlParseCache((cache) -> cache.maximumSize(1024).expireAfterWrite(5, TimeUnit.SECONDS)));
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.ORACLE)); // 分页插件
        return mybatisPlusInterceptor;
    }

    /**
     * 注册 Oracle LocalDateTime TypeHandler
     * 
     * 根本原因：MyBatis 默认的 LocalDateTimeTypeHandler 使用 ResultSet.getObject() 读取数据，
     * Oracle JDBC 驱动（21.9.0.0）在尝试将 TIMESTAMP 转换为 LocalDateTime 时调用 getLocalDateTime() 失败（ORA-17132）。
     * 
     * 解决方案：使用自定义的 OracleLocalDateTimeTypeHandler，它使用 ResultSet.getTimestamp() 读取数据，
     * 然后转换为 LocalDateTime，避免直接调用 Oracle 驱动的 getLocalDateTime() 方法。
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (applicationContext.getParent() != null) {
            log.debug("========== 忽略子上下文事件，跳过 TypeHandler 注册 ==========");
            return;
        }
        
        log.info("========== 开始注册 OracleLocalDateTimeTypeHandler ==========");
        Collection<SqlSessionFactory> sqlSessionFactories = applicationContext.getBeansOfType(SqlSessionFactory.class).values();
        
        if (sqlSessionFactories == null || sqlSessionFactories.isEmpty()) {
            log.warn("========== 未找到 SqlSessionFactory，无法注册 TypeHandler ==========");
            return;
        }
        
        log.info("========== 找到 {} 个 SqlSessionFactory ==========", sqlSessionFactories.size());
        
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            
            // 配置 Oracle 的 NULL 值处理
            // Oracle 不支持 JdbcType.OTHER (类型代码 1111) 来设置 NULL 值
            // 设置为 NULL 类型，让 Oracle 驱动自动处理
            configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
            log.info("========== 已设置 jdbcTypeForNull: {} ==========", org.apache.ibatis.type.JdbcType.NULL);
            
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            
            // 检查注册前的状态
            org.apache.ibatis.type.TypeHandler<?> beforeHandler = typeHandlerRegistry.getTypeHandler(
                java.time.LocalDateTime.class);
            log.info("========== 注册前 LocalDateTime 的 TypeHandler: {} ==========", 
                beforeHandler != null ? beforeHandler.getClass().getName() : "null");
            
            // 注册带 jdbcType 的 TypeHandler
            log.info("========== 注册 LocalDateTime + TIMESTAMP 的 TypeHandler ==========");
            typeHandlerRegistry.register(java.time.LocalDateTime.class, 
                org.apache.ibatis.type.JdbcType.TIMESTAMP, 
                OracleLocalDateTimeTypeHandler.class);
            
            log.info("========== 注册 LocalDateTime + DATE 的 TypeHandler ==========");
            typeHandlerRegistry.register(java.time.LocalDateTime.class, 
                org.apache.ibatis.type.JdbcType.DATE, 
                OracleLocalDateTimeTypeHandler.class);
            
            // 注册仅按 Java 类型的 TypeHandler，覆盖默认的 LocalDateTimeTypeHandler
            // 这是关键：当 MyBatis 查询时没有明确 jdbcType 时，会使用这个
            // 注意：MyBatis 的 register(Class, TypeHandler) 方法可能无法覆盖已注册的默认 TypeHandler
            // 因此需要使用反射直接修改内部的 Map
            log.info("========== 尝试通过反射直接覆盖默认的 LocalDateTime TypeHandler ==========");
            boolean reflectionSuccess = false;
            try {
                // 尝试多个可能的字段名
                String[] possibleFieldNames = {"TYPE_HANDLER_MAP", "typeHandlerMap", "allTypeHandlersMap"};
                Field typeHandlerMapField = null;
                
                for (String fieldName : possibleFieldNames) {
                    try {
                        typeHandlerMapField = TypeHandlerRegistry.class.getDeclaredField(fieldName);
                        log.info("========== 找到字段: {} ==========", fieldName);
                        break;
                    } catch (NoSuchFieldException e) {
                        log.debug("========== 字段 {} 不存在，尝试下一个 ==========", fieldName);
                    }
                }
                
                if (typeHandlerMapField == null) {
                    // 如果所有字段名都失败，列出所有字段用于调试
                    log.warn("========== 未找到 TYPE_HANDLER_MAP 字段，列出所有字段用于调试 ==========");
                    Field[] allFields = TypeHandlerRegistry.class.getDeclaredFields();
                    for (Field field : allFields) {
                        log.warn("========== 字段: {} (类型: {}) ==========", field.getName(), field.getType().getName());
                    }
                    throw new NoSuchFieldException("未找到 TYPE_HANDLER_MAP 相关字段");
                }
                
                typeHandlerMapField.setAccessible(true);
                Object typeHandlerMapObj = typeHandlerMapField.get(typeHandlerRegistry);
                
                if (typeHandlerMapObj == null) {
                    throw new IllegalStateException("typeHandlerMap 字段值为 null");
                }
                
                log.info("========== typeHandlerMap 的实际类型: {} ==========", typeHandlerMapObj.getClass().getName());
                
                // 创建 OracleLocalDateTimeTypeHandler 实例
                OracleLocalDateTimeTypeHandler handlerInstance = new OracleLocalDateTimeTypeHandler();
                
                // MyBatis 的 typeHandlerMap 是嵌套结构：Map<Class<?>, Map<JdbcType, TypeHandler<?>>>
                // 需要先获取内层 Map，然后放入 TypeHandler
                @SuppressWarnings("unchecked")
                Map<Class<?>, Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>>> typeHandlerMap = 
                    (Map<Class<?>, Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>>>) typeHandlerMapObj;
                
                // 获取或创建 LocalDateTime 的内层 Map
                Map<org.apache.ibatis.type.JdbcType, org.apache.ibatis.type.TypeHandler<?>> innerMap = 
                    typeHandlerMap.computeIfAbsent(java.time.LocalDateTime.class, k -> new java.util.HashMap<>());
                
                // 将自定义 TypeHandler 放入内层 Map，key 为 null 表示默认（无 jdbcType 时使用）
                org.apache.ibatis.type.TypeHandler<?> oldHandler = innerMap.put(null, handlerInstance);
                log.info("========== 通过反射覆盖成功（null jdbcType），旧的 TypeHandler: {} ==========", 
                    oldHandler != null ? oldHandler.getClass().getName() : "null");
                
                // 同时也覆盖 TIMESTAMP 和 DATE，确保一致性
                innerMap.put(org.apache.ibatis.type.JdbcType.TIMESTAMP, handlerInstance);
                innerMap.put(org.apache.ibatis.type.JdbcType.DATE, handlerInstance);
                
                reflectionSuccess = true;
            } catch (Exception e) {
                log.error("========== 反射覆盖失败: {} ==========", e.getMessage(), e);
                log.warn("========== 回退到使用 register 方法 ==========");
                // 如果反射失败，回退到 register 方法
                typeHandlerRegistry.register(java.time.LocalDateTime.class, 
                    OracleLocalDateTimeTypeHandler.class);
            }
            
            // 验证注册是否成功
            org.apache.ibatis.type.TypeHandler<?> afterHandler = typeHandlerRegistry.getTypeHandler(
                java.time.LocalDateTime.class);
            org.apache.ibatis.type.TypeHandler<?> timestampHandler = typeHandlerRegistry.getTypeHandler(
                java.time.LocalDateTime.class, org.apache.ibatis.type.JdbcType.TIMESTAMP);
            org.apache.ibatis.type.TypeHandler<?> dateHandler = typeHandlerRegistry.getTypeHandler(
                java.time.LocalDateTime.class, org.apache.ibatis.type.JdbcType.DATE);
            
            log.info("========== 注册后 LocalDateTime 的 TypeHandler（无 jdbcType）: {} ==========", 
                afterHandler != null ? afterHandler.getClass().getName() : "null");
            log.info("========== 注册后 LocalDateTime + TIMESTAMP 的 TypeHandler: {} ==========", 
                timestampHandler != null ? timestampHandler.getClass().getName() : "null");
            log.info("========== 注册后 LocalDateTime + DATE 的 TypeHandler: {} ==========", 
                dateHandler != null ? dateHandler.getClass().getName() : "null");
            
            if (afterHandler != null && afterHandler.getClass() == OracleLocalDateTimeTypeHandler.class) {
                log.info("========== ✅ OracleLocalDateTimeTypeHandler 注册成功（按 Java 类型） ==========");
            } else {
                log.error("========== ❌ OracleLocalDateTimeTypeHandler 注册失败！当前使用的 TypeHandler: {} ==========", 
                    afterHandler != null ? afterHandler.getClass().getName() : "null");
            }
        }
        
        log.info("========== OracleLocalDateTimeTypeHandler 注册完成 ==========");
    }

    @Bean
    public MetaObjectHandler defaultMetaObjectHandler() {
        return new DefaultDBFieldHandler(); // 自动填充参数类
    }

    /**
     * 使用redis生成唯一的Work ID
     * 
     * @param inetUtils
     * @param redisson
     * @return
     */
    @Bean
    public RedisWorker redisWorker(InetUtils inetUtils, RedissonClient redisson) {
        return new RedisWorker(inetUtils, redisson, WorkerOnlySnowflakeIdGenerator.MAX_WORK_ID);
    }

    /**
     * 雪花ID
     * 
     * @param redisWorker
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator identifierGenerator(RedisWorker redisWorker) {
        return new WorkerOnlySnowflakeIdGenerator(redisWorker.getId());
    }

    @Bean
    @ConditionalOnProperty(prefix = "mybatis-plus.global-config.db-config", name = "id-type", havingValue = "INPUT")
    public IKeyGenerator keyGenerator(ConfigurableEnvironment environment) {
        DbType dbType = IdTypeEnvironmentPostProcessor.getDbType(environment);
        if (dbType != null) {
            switch (dbType) {
            case POSTGRE_SQL:
                return new PostgreKeyGenerator();
            case ORACLE_12C, ORACLE:
                return new OracleKeyGenerator();
            case H2:
                return new H2KeyGenerator();
            case KINGBASE_ES:
                return new KingbaseKeyGenerator();
            case DM:
                return new DmKeyGenerator();
            default:
                throw new IllegalStateException("Unexpected value: " + dbType);
            }
        }
        // 找不到合适的 IKeyGenerator 实现类
        throw new IllegalArgumentException(CharSequenceUtil.format("DbType{} 找不到合适的 IKeyGenerator 实现类", dbType));
    }

}
