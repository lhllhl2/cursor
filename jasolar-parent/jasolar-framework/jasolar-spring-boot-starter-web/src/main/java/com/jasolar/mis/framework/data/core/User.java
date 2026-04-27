package com.jasolar.mis.framework.data.core;

import com.jasolar.mis.framework.common.enums.CommonStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 人员信息
 * 
 * @author galuo
 * @date 2025-03-28 11:48
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends SimpleUser implements Serializable {

    /** 法人代码 */
    private String legalCode;

    /** 法人名称 */
    private String legalName;

    /** 部门代码,即费用代码 */
    private String deptCode;

    /** 部门名称 */
    private String deptName;

    /** 事业群CODE */
    private String businessGroupCode;
    /** 事业群名称;冗余字段 */
    private String businessGroupName;
    /** 事业处CODE */
    private String businessUnitCode;
    /** 事业处名称;冗余字段 */
    private String businessUnitName;

    /** 有效状态, 参见 {@link CommonStatusEnum} 枚举 */
    private int status;

    /** 邮箱 */
    private String email;

    /** 手机号码 */
    private String mobile;

}
