package com.jasolar.mis.module.system.controller.budget.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/12/2025 14:12
 * Version : 1.0
 */
@Data
@ToString
public class BudgetQueryOrgRelationsParams {

    @NotNull(message = "ESB信息不能为空")
    private ESBInfoParams esbInfo;

}
