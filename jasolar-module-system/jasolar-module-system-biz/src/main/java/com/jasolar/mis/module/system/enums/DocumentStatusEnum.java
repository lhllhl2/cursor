package com.jasolar.mis.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description: 单据状态枚举
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {

    /** 未提交 */
    NOT_SUBMITTED("NOT_SUBMITTED", "未提交"),

    /** 已更新 */
    UPDATED("UPDATED", "已更新"),

    /** 初始提交 */
    INITIAL_SUBMITTED("INITIAL_SUBMITTED", "初始提交"),

    /** 已提交 */
    SUBMITTED("SUBMITTED", "已提交"),

    /** 待审批 */
    PENDING("PENDING", "待审批"),

    /** 已审批 */
    APPROVED("APPROVED", "已审批"),

    /** 已拒绝 */
    REJECTED("REJECTED", "已拒绝"),

    /** 已取消 */
    CANCELLED("CANCELLED", "已取消"),

    /** 明细删除（按明细维度逻辑删除并回滚） */
    DETAIL_DELETED("DETAIL_DELETED", "明细删除");

    /**
     * 状态值
     */
    private final String code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据状态值获取枚举
     *
     * @param code 状态值
     * @return 枚举对象，如果未找到返回null
     */
    public static DocumentStatusEnum valueOfCode(String code) {
        if (code == null) {
            return null;
        }
        for (DocumentStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断状态值是否有效
     *
     * @param code 状态值
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return valueOfCode(code) != null;
    }
}

