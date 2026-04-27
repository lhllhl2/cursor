package com.jasolar.mis.framework.datapermission.core.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;
import com.jasolar.mis.framework.datapermission.core.annotation.EnableDataPermission;
import com.jasolar.mis.framework.test.core.ut.BaseMockitoUnitTest;

import cn.hutool.core.collection.CollUtil;

/**
 * {@link DataPermissionAnnotationInterceptor} 的单元测试
 *
 * @author zhaohuang
 */
class DataPermissionAnnotationInterceptorTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DataPermissionAnnotationInterceptor interceptor;

    @Mock
    private MethodInvocation methodInvocation;

    @BeforeEach
    public void setUp() {
        interceptor.getCaches().clear();
    }

    @Test // 无 @DataPermission 注解
    void testInvoke_none() throws Throwable {
        // 参数
        mockMethodInvocation(TestNone.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("none", result);
        assertEquals(1, interceptor.getCaches().size());
        assertNull(CollUtil.getFirst(interceptor.getCaches().values()).getEnabled());
    }

    @Test // 在 Method 上有 @DataPermission 注解
    void testInvoke_method() throws Throwable {
        // 参数
        mockMethodInvocation(TestMethod.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("method", result);
        assertEquals(1, interceptor.getCaches().size());
        assertFalse(CollUtil.getFirst(interceptor.getCaches().values()).getEnabled());
    }

    @Test // 在 Class 上有 @DataPermission 注解
    void testInvoke_class() throws Throwable {
        // 参数
        mockMethodInvocation(TestClass.class);

        // 调用
        Object result = interceptor.invoke(methodInvocation);
        // 断言
        assertEquals("class", result);
        assertEquals(1, interceptor.getCaches().size());
        assertFalse(CollUtil.getFirst(interceptor.getCaches().values()).getEnabled());
    }

    private void mockMethodInvocation(Class<?> clazz) throws Throwable {
        Object targetObject = clazz.newInstance();
        Method method = targetObject.getClass().getMethod("echo");
        when(methodInvocation.getThis()).thenReturn(targetObject);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.proceed()).then(invocationOnMock -> method.invoke(targetObject));
    }

    static class TestMethod {

        @DataPermission()
        @EnableDataPermission(false)
        public String echo() {
            return "method";
        }

    }

    @EnableDataPermission(false)
    static class TestClass {

        public String echo() {
            return "class";
        }

    }

    static class TestNone {

        public String echo() {
            return "none";
        }

    }

}
