package com.jasolar.mis.framework.datapermission.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import com.jasolar.mis.framework.common.util.json.JsonUtils;
import com.jasolar.mis.framework.data.util.PermissionUtils;
import com.jasolar.mis.framework.datapermission.core.annotation.FieldPermission;
import com.jasolar.mis.module.system.api.permission.PermissionApi;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Data;

/**
 * {@link FieldPermissionTest} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
public class FieldPermissionTest {

    @Test
    public void test() {
        // 准备参数
        DesensitizeDemo desensitizeDemo = new DesensitizeDemo();
        desensitizeDemo.setNickname("黄朝晖");
        desensitizeDemo.setName("name");

        ApplicationContext ctx = Mockito.mock(ApplicationContext.class);
        PermissionApi ss = Mockito.mock(PermissionApi.class);
        RedissonClient redisson = Mockito.mock(RedissonClient.class);

        SpringUtil s = new SpringUtil();
        s.setApplicationContext(ctx);

        Mockito.when(ctx.getBean(Mockito.eq(PermissionApi.class))).thenReturn(ss);
        PermissionUtils.REDISSON = redisson;
        PermissionUtils.init();

        @SuppressWarnings("unchecked")
        RSet<Object> set = Mockito.mock(RSet.class);
        Mockito.when(redisson.getSet(anyString())).thenReturn(set);

        // 调用
        DesensitizeDemo d = JsonUtils.parseObject(JsonUtils.toJsonString(desensitizeDemo), DesensitizeDemo.class);
        // 断言
        assertNotNull(d);
        assertNull(d.getNickname());
        assertEquals("name", d.getName());

    }

    @Data
    public static class DesensitizeDemo {
        @FieldPermission("system.nickname")
        private String nickname;
        private String name;
    }

}
