package com.jasolar.mis.framework.common.security;

import java.io.Serializable;

import lombok.Data;

/**
 * 供应商信息
 * 
 * @author galuo
 * @date 2025-03-17 16:13
 *
 */
@SuppressWarnings("serial")
@Data
public class LoginSupplier implements Serializable {

    /** 注册未审批通过, 注册已审批通过, 合格(已准入), 冻结下单,冻结付款,完全冻结 */
    private String status;

    /** 准入供应商编码 */
    private String code;

}
