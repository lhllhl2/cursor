package com.jasolar.mis.framework.mybatis.core.handler;

import java.time.LocalDateTime;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import com.jasolar.mis.framework.web.core.util.WebFrameworkUtils;

/**
 * 通用参数填充实现类
 *
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author hexiaowu
 */
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject == null || !(metaObject.getOriginalObject() instanceof BaseDO)) {
            return;
        }
        BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
        // 允许外部传创建时间和创建人
        if (Objects.isNull(baseDO.getCreateTime())) {
            baseDO.setCreateTime(LocalDateTime.now());
        }

        String userNo = WebFrameworkUtils.getLoginUserNo();
        if (StringUtils.isNotBlank(userNo)) {
            if (StringUtils.isBlank(baseDO.getCreator())) {
                baseDO.setCreator(userNo);
            }
        }

        // 更新人
        baseDO.setUpdater(baseDO.getCreator());
        // 更新时间
        baseDO.setUpdateTime(LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // // 更新时间为空，则以当前时间为更新时间
        // Object modifyTime = getFieldValByName("updateTime", metaObject);
        // if (Objects.isNull(modifyTime)) {
        // setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        // }
        //
        // // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
        // // Object modifier = getFieldValByName("updater", metaObject);
        // String userNo = WebFrameworkUtils.getLoginUserNo();
        // if (StringUtils.isNotBlank(userNo)) {
        // setFieldValByName("updater", userNo, metaObject);
        // }

        if (metaObject == null || !(metaObject.getOriginalObject() instanceof BaseDO)) {
            return;
        }

        BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
        baseDO.setUpdateTime(LocalDateTime.now());
        baseDO.setUpdater(WebFrameworkUtils.getLoginUserNo());
    }
}
