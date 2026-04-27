package com.jasolar.mis.module.system.oauth;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
@Data
@Builder
public class VerifyResponse {

    private static final long serialVersionUID = -9146647138495276470L;

    private boolean success;

    private UserResponse user;

}