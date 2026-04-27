package com.jasolar.mis.module.system.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 28/11/2025 15:34
 * Version : 1.0
 */
@Slf4j
public class DistributedLockUtils {


   public static void lock(RedissonClient redissonClient, String key, RunFunction runFunction){
      RLock lock = redissonClient.getLock(key);
      try {
         boolean b = lock.tryLock(10, 60, TimeUnit.SECONDS);
         if(!b){
            return;
         }
         try {
            runFunction.work();
         }finally {
            lock.unlock();
         }
      }catch (InterruptedException e){
         Thread.currentThread().interrupt();
         log.info("线程被中断,正在退出。。。");
      }
   }



}
