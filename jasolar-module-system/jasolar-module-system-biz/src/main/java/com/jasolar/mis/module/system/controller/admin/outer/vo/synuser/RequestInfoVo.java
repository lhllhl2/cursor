package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 16:10
 * Version : 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class RequestInfoVo {

    private String id;

    /**
     * 账户名称,唯一
     */
    @NotBlank(message = "工号不能为空")
    private String userName;

    // 用户的显示名称,唯一
    private String displayName;

    // 邮箱
    private String emails;

    // 手机区号
    private String phoneRegion;

    // phoneNumber
    private String phoneNumbers;

    // 外部ID,唯一,不为空
//    private String externalId;

    // true:启用，false:禁用，默认为true。
    private Boolean enabled;

    // 是否禁用账户，ture禁用账户,false启用账户。禁用账户后将不能登录应用系统
    private Boolean locked;

    // 属于的组织 【】
    private List<BelongVo> belongs;

    /**
     * 扩展字段,attributes为系统定义扩展字段
     */
    private ExtendFieldVo extendField;




}
