package com.jasolar.mis.module.system.oauth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 18/07/2025 13:48
 * Version : 1.0
 */
@Data
public class SsoCallbackParams {

    @NotBlank(message = "code不能为空！")
    private String code;

    private String state;



}
