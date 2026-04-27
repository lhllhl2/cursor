package com.jasolar.mis.framework.apilog.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.jasolar.mis.module.log.api.ApiAccessLogApi;
import com.jasolar.mis.module.log.api.ApiErrorLogApi;

/**
 * API 日志使用到 Feign 的配置项
 *
 * @author zhaohuang
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "jasolar.access-log", value = "enable", matchIfMissing = true)
@EnableFeignClients(clients = { ApiAccessLogApi.class, ApiErrorLogApi.class }) // 主要是引入相关的 API 服务
public class JasolarApiLogRpcAutoConfiguration {

}
