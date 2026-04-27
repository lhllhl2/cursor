package com.jasolar.mis.framework.datapermission.config;

import java.util.List;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.datapermission.core.rule.DataPermissionRuleCustomizer;
import com.jasolar.mis.framework.datapermission.core.rule.impl.BpmTaskDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.BusinessGroupDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.BusinessUnitDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.DeptDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.LegalDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.PublicWorkDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.SupplierPortalDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.rule.impl.UserDataPermissionRule;
import com.jasolar.mis.framework.datapermission.core.service.DataScopeService;
import com.jasolar.mis.framework.datapermission.core.service.DataScopeServiceImpl;
import com.jasolar.mis.module.system.api.permission.PermissionApi;

import cn.hutool.extra.spring.SpringUtil;

/**
 * 基于部门的数据权限 AutoConfiguration
 *
 * @author zhaohuang
 */
@AutoConfiguration
@ConditionalOnClass(LoginUser.class)
public class JasolarDataPermissionRuleAutoConfiguration {

    /**
     * DataPermissionService的初始化
     * 
     * @param permissionApi 用于feign调用权限API的对象,在system服务中,使用permissionApiImpl替换
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    DataScopeService dataScopeService(PermissionApi permissionApi, RedissonClient redisson,
            @Value("${jasolar.permission.data.redis.cache-ttl:36000}") int redisCacheTtl,
            @Value("${jasolar.permission.data.memory.cache-ttl:60}") int memoryCacheTtl,
            @Value("${jasolar.permission.data.memory.cache-count:1000}") int memoryCacheCount) {
        // Cloud 专属逻辑：优先使用本地的 PermissionApi 实现类，而不是 Feign 调用
        // 原因：在创建租户时，租户还没创建好，导致 Feign 调用获取数据权限时，报“租户不存在”的错误
        try {
            PermissionApi permissionApiImpl = SpringUtil.getBean("permissionApiImpl", PermissionApi.class);
            if (permissionApiImpl != null) {
                permissionApi = permissionApiImpl;
            }
        } catch (Exception ignored) {
        }

        return new DataScopeServiceImpl(permissionApi, redisson, redisCacheTtl, memoryCacheTtl, memoryCacheCount);

    }

    /**
     * 供应商门户端的权限控制
     * 
     * @param customizers
     * @return
     */
    @Bean
    public SupplierPortalDataPermissionRule supplierPortalDataPermissionRule(
            List<DataPermissionRuleCustomizer<SupplierPortalDataPermissionRule>> customizers) {
        SupplierPortalDataPermissionRule rule = new SupplierPortalDataPermissionRule();
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 人员维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public UserDataPermissionRule userDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<UserDataPermissionRule>> customizers) {
        UserDataPermissionRule rule = new UserDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * BPM审批人员维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public BpmTaskDataPermissionRule bpmUserDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<BpmTaskDataPermissionRule>> customizers) {
        BpmTaskDataPermissionRule rule = new BpmTaskDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 部门维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public DeptDataPermissionRule deptDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<DeptDataPermissionRule>> customizers) {
        DeptDataPermissionRule rule = new DeptDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 法人维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public LegalDataPermissionRule legalDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<LegalDataPermissionRule>> customizers) {
        LegalDataPermissionRule rule = new LegalDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 事业群维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public BusinessGroupDataPermissionRule businessGroupDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<BusinessGroupDataPermissionRule>> customizers) {
        BusinessGroupDataPermissionRule rule = new BusinessGroupDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 事业处维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public BusinessUnitDataPermissionRule businessUnitDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<BusinessUnitDataPermissionRule>> customizers) {
        BusinessUnitDataPermissionRule rule = new BusinessUnitDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }

    /**
     * 工务窗口维度的数据权限
     * 
     * @param dataPermissionService
     * @param customizers
     * @return
     */
    @Bean
    public PublicWorkDataPermissionRule publicWorkDataPermissionRule(DataScopeService dataPermissionService,
            List<DataPermissionRuleCustomizer<PublicWorkDataPermissionRule>> customizers) {
        PublicWorkDataPermissionRule rule = new PublicWorkDataPermissionRule();
        rule.setDataPermissionService(dataPermissionService);
        customizers.stream().filter(c -> c.accept(rule)).forEach(customizer -> customizer.customize(rule));
        return rule;
    }
}
