package com.jasolar.mis.module.system.schedule;

import com.jasolar.mis.module.system.service.ehr.EhrOrgManageRService;
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
 * Date : 11/12/2025 17:23
 * Version : 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "scheduler.organization-sync.enabled", havingValue = "true", matchIfMissing = false)
public class EhrOrgSyncSchedule {


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EhrOrgManageRService ehrOrgManageRService;


    /**
     * 定时同步原始组织数据到业务组织表
     *
     * 每天凌晨1点执行一次
     */
    @Scheduled(cron = "${organization-sync.cron:0 0 1 * * ?}")
    public void syncOriginalOrganizationToBusiness() {

        DistributedLockUtils.lock(redissonClient,"org_syn_key",() -> {
            log.info("开始执行原始组织数据同步任务");
            ehrOrgManageRService.syncProjectToBusiness();

            // 执行原始组织数据同步
            log.info("原始组织数据同步任务执行完成");
        });
    }


}
