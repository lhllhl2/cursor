package com.jasolar.mis.module.system.threadpool.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 20/08/2025 17:42
 * Version : 1.0
 */
@Slf4j
public class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final GlobalUncaughtExceptionHandler instance = new GlobalUncaughtExceptionHandler();

    private GlobalUncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread {} ", t.getName(), e);
    }

    public static GlobalUncaughtExceptionHandler getInstance() {
        return instance;
    }

}