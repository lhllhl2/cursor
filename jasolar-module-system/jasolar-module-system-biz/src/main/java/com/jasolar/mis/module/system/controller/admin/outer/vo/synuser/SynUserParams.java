package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 17:21
 * Version : 1.0
 */
@Data
@ToString
public class SynUserParams {

    @NotNull(message = "esbInfo 不能为空！ ")
    private EsbInfoVo esbInfo;

    @NotNull(message = "用户信息不能为空")
    private RequestInfoVo requestInfo;





}
