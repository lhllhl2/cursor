package com.jasolar.mis.framework.common.enums;

import java.util.Arrays;

import com.jasolar.mis.framework.common.core.ArrayValuable;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author zhaohuang
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum implements ArrayValuable<Integer> {

    ENABLE(0, "开启"), DISABLE(1, "关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CommonStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isEnable(Integer status) {
        return ObjectUtil.equal(ENABLE.status, status);
    }

    public static boolean isDisable(Integer status) {
        return ObjectUtil.equal(DISABLE.status, status);
    }

    /**
     * 得到枚举对象
     * 
     * @param status 状态
     * @return 与参数{@code status}匹配的枚举, 如果无法匹配则返回{@link #DISABLE}
     */
    public static CommonStatusEnum valueOf(Integer status) {
        if (status == null) {
            return CommonStatusEnum.DISABLE;
        }
        for (CommonStatusEnum e : CommonStatusEnum.values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return CommonStatusEnum.DISABLE;
    }

}
