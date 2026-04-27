package com.jasolar.mis.module.system.schedule;

import com.jasolar.mis.module.system.service.ehr.ProjectControlRService;
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
 * Date : 15/12/2025 9:41
 * Version : 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "scheduler.project-sync.enabled", havingValue = "true", matchIfMissing = false)
public class ProjectSyncSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProjectControlRService projectControlRService;


    @Scheduled(cron = "${project-sync.cron:0 0 1 * * ?}")
    public void syncProjectToBusiness() {

        DistributedLockUtils.lock(redissonClient,"project_syn_key",() -> {
            log.info("开始执行项目数据同步任务");

            projectControlRService.syncProjectToBusiness();

            // 执行原始组织数据同步
            log.info("项目数据同步任务执行完成");
        });
    }


}
