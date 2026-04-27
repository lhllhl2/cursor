package com.jasolar.mis.module.system.oauth;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 05/09/2025 16:35
 * Version : 1.0
 */
@Data
@SuperBuilder
public class LocalLoginResponse extends TokenResponse{

    public Boolean needChanged;


}
