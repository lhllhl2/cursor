package com.jasolar.mis.framework.datapermission.core.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jasolar.mis.framework.common.util.collection.SetUtils;
import com.jasolar.mis.framework.datapermission.core.annotation.DataPermission;

/**
 * {@link DataPermissionContextHolder} 的单元测试
 *
 * @author zhaohuang
 */
class DataPermissionContextHolderTest {

    @BeforeEach
    public void setUp() {
        DataPermissionContextHolder.clear();
    }

    @Test
    void testGet() {
        // mock 方法
        DataPermission[] dataPermission01 = new DataPermission[] { mock(DataPermission.class) };
        DataPermissionContextHolder.add(SetUtils.asSet(dataPermission01));
        DataPermission[] dataPermission02 = new DataPermission[] { mock(DataPermission.class) };
        Set<DataPermission> set = SetUtils.asSet(dataPermission02);

        Assertions.assertTrue(DataPermissionContextHolder.add(SetUtils.asSet(dataPermission02)));

        // 调用
        DataPermissionContext r = DataPermissionContextHolder.get();
        Assertions.assertTrue(r.hasRule());

        // 断言
        assertEquals(r.getPermissions(), set);

        DataPermissionContextHolder.enable(false);
        Assertions.assertTrue(DataPermissionContextHolder.isDisabled());

        DataPermission[] aa = new DataPermission[] { mock(DataPermission.class) };
        Assertions.assertFalse(DataPermissionContextHolder.add(SetUtils.asSet(aa)));

        DataPermissionContextHolder.enable(true);
        Assertions.assertFalse(DataPermissionContextHolder.isDisabled());

        aa = new DataPermission[] { mock(DataPermission.class) };
        Set<DataPermission> seta = SetUtils.asSet(aa);
        Assertions.assertTrue(DataPermissionContextHolder.add(seta));

        DataPermissionContextHolder.remove();
        DataPermissionContextHolder.remove();
        DataPermissionContextHolder.remove();
        DataPermissionContextHolder.remove();
        DataPermissionContextHolder.remove();
        Assertions.assertNull(DataPermissionContextHolder.get());

    }

}
