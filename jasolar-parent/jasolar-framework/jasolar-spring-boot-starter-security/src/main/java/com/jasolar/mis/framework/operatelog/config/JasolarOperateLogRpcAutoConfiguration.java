package com.jasolar.mis.framework.operatelog.config;

import com.jasolar.mis.module.log.api.OperateLogApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * OperateLog 使用到 Feign 的配置项
 *
 * @author zhaohuang
 */
@AutoConfiguration
@EnableFeignClients(clients = {OperateLogApi.class}) // 主要是引入相关的 API 服务
public class JasolarOperateLogRpcAutoConfiguration {
}
