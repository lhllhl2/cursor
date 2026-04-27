package com.jasolar.mis.gateway.resp;

import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 17/07/2025 18:49
 * Version : 1.0
 */
@Data
@Builder
public class GlobalResponse {

    private int code;

    private String msg;

    private String data;

}
