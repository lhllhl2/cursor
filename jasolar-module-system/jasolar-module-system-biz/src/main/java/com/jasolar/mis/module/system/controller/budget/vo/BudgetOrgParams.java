package com.jasolar.mis.module.system.controller.budget.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * Description: 预算组织参数
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@ToString
public class BudgetOrgParams {

    @NotNull(message = "ESB信息不能为空")
    private ESBInfoParams esbInfo;
}

