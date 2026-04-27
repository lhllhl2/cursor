package com.jasolar.mis.module.system.schedule;

import com.jasolar.mis.module.system.service.admin.project.SystemProjectService;
import com.jasolar.mis.module.system.util.DistributedLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 09/01/2026 11:26
 * Version : 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "scheduler.auth-project-sync.enabled", havingValue = "true", matchIfMissing = false)
public class AuthProjectSyncSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SystemProjectService systemProjectService;

    @Scheduled(cron = "${auth-project-sync.cron:0 0 1 * * ?}")
    public void syncAuthProjectToBusiness() {

        DistributedLockUtils.lock(redissonClient,"auth-project_syn_key",() -> {
            log.info("开始执行权限项目数据同步任务");

            systemProjectService.syncProjectToBusiness();

            // 执行原始组织数据同步
            log.info("权限项目数据同步任务执行完成");
        });
    }

}
