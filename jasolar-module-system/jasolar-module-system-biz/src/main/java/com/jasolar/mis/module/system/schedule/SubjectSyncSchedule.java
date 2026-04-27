package com.jasolar.mis.module.system.schedule;

import com.jasolar.mis.module.system.service.ehr.SubjectInfoService;
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
 * Date : 16/12/2025 10:15
 * Version : 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "scheduler.subject-sync.enabled", havingValue = "true", matchIfMissing = false)
public class SubjectSyncSchedule {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SubjectInfoService subjectInfoService;


    @Scheduled(cron = "${subject-sync.cron:0 0 2 * * ?}")
    public void syncSubjectToBusiness() {

        DistributedLockUtils.lock(redissonClient,"subject_syn_key",() -> {
            log.info("开始执行科目数据同步任务");

            subjectInfoService.syncSubjectToBusiness();

            // 执行原始组织数据同步
            log.info("科目数据同步任务执行完成");
        });
    }


}