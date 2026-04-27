package com.jasolar.mis.framework.datapermission.core.field;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.framework.data.util.PermissionUtils;
import com.jasolar.mis.framework.datapermission.core.annotation.FieldPermission;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 数据字段权限默认处理器
 *
 * @author zhangj
 */
public class DefaultFieldPermissionHandler implements FieldPermissionHandler {

    @Override
    public boolean hasPermission(Object origin, FieldPermission annotation) {
        LoginUser user = LoginServletUtils.getLoginUser();
        if (user == null || !user.isAuthorized()) {
            return false;
        }
        // 如果是供应商账号,直接视为无权限. 因为供应商账号没有后台账号,无法分配权限.
        if (UserTypeEnum.SUPPLIER == user.userType()) {
            return false;
        }

        String value = annotation.value();
        return CharSequenceUtil.isBlank(value) || PermissionUtils.hasAnyPermission(user.getNo(), value);
    }

}
