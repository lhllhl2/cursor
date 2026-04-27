package com.jasolar.mis.framework.datapermission.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.jasolar.mis.framework.datapermission.core.aop.DataPermissionContextHolder;

class DataPermissionUtilsTest {

    @Test
    void testExecuteIgnore() {
        assertFalse(DataPermissionContextHolder.isDisabled());
        DataPermissionUtils.executeIgnore(() -> assertTrue(DataPermissionContextHolder.isDisabled()));
    }

}
