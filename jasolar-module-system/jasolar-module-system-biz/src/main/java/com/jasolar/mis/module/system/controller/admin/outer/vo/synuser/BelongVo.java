package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import lombok.Data;
import lombok.ToString;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 16:17
 * Version : 1.0
 */
@Data
@ToString
public class BelongVo {

    // 具体的组织外部id
    private String belong;

    // 是否主组织
    //true:是,false:不是
    private Boolean mainOu;

    // TODO : 需要正确的类型
//    private String extendFields;

    private String belongOuUuid;



}
