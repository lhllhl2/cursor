package com.jasolar.mis.framework.data.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jasolar.mis.framework.data.RedissonUtils;
import com.jasolar.mis.framework.redis.lock.RedisLockExecutor;
import com.jasolar.mis.module.masterdata.api.consignCenter.ConsignCenterApi;
import com.jasolar.mis.module.masterdata.api.material.MaterialApi;
import com.jasolar.mis.module.supplier.api.admin.SupplierAdminApi;
import com.jasolar.mis.module.system.api.permission.PermissionApi;

/**
 * 系统主数据
 * 
 * @author galuo
 * @date 2025-04-02 09:30
 *
 */
@AutoConfiguration
public class DataAutoConfiguration {

    /**
     * 初始化redisson客户端, 用于主数据缓存
     * 
     * @param redisson
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "jasolar.data", value = "enable", matchIfMissing = true)
    public RedissonUtils redissonUtils(RedissonClient redisson, RedisLockExecutor lockExecutor) {
        return new RedissonUtils(redisson, lockExecutor);
    }

    /**
     * 配置System服务的FeignClient
     * 
     * @author galuo
     * @date 2025-04-02 09:39
     *
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "jasolar.data.api", value = "system", matchIfMissing = true)
    @EnableFeignClients(clients = { PermissionApi.class })
    static class SystemDataApiFeignConfiguration {

    }

    /**
     * 配置Masterdata服务的FeignClient
     * 
     * @author galuo
     * @date 2025-04-02 09:39
     *
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "jasolar.data.api", value = "masterdata", matchIfMissing = true)
    @EnableFeignClients(clients = { MaterialApi.class, ConsignCenterApi.class })
    static class MasterDataApiFeignConfiguration {

    }

    /**
     * 配置Supplier服务的FeignClient
     * 
     * @author galuo
     * @date 2025-04-02 09:39
     *
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "jasolar.data.api", value = "supplier", matchIfMissing = true)
    @EnableFeignClients(clients = { SupplierAdminApi.class })
    static class SupplierDataApiFeignConfiguration {

    }

    // /**
    // * 配置BPM服务的FeignClient
    // *
    // * @author galuo
    // * @date 2025-04-02 09:39
    // *
    // */
    // @Configuration(proxyBeanMethods = false)
    // @ConditionalOnProperty(prefix = "jasolar.data.api", value = "bpm", matchIfMissing = true)
    // @EnableFeignClients(clients = { BpmProcessInstanceApi.class })
    // static class BpmApiFeignConfiguration {
    //
    // }

}
