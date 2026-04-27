package com.jasolar.mis.framework.common.security;

import java.io.Serializable;

import lombok.Data;

/**
 * 采购平台B端用户
 * 
 * @author galuo
 * @date 2025-03-17 16:14
 *
 */
@Data
@SuppressWarnings("serial")
public class LoginAdminUser implements Serializable {

    /** 部门编号 */
    private String deptCode;

    /** 法人编号 */
    private String legalCode;

    /** 事业处 */
    private String businessUnitCode;

    /** 事业群 */
    private String businessGroupCode;

}
