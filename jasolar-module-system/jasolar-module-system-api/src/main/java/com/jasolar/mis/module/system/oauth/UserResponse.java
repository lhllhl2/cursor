package com.jasolar.mis.module.system.oauth;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Liu Bingjian
 */
@Data
@Builder
public class UserResponse implements Serializable {

    private static final long serialVersionUID = -2440032474884158265L;

    private Long id;

    private String userName;

    private String displayName;


}
