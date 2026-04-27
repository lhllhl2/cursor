package com.jasolar.mis.framework.gateway.util;

import cn.hutool.core.thread.ThreadUtil;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author zhaohuang
 */
@Component
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾

            log.info("----------------------------------------------------------[" +
                    SpringUtils.getApplicationName() + "]项目启动成功！" +
                    "----------------------------------------------------------");

        });
    }

}
