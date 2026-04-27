package com.jasolar.mis.module.system.controller.admin.outer.vo.synuser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 16:07
 * Version : 1.0
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class EsbInfoVo {

    //
    private String requestTime;

    // 该字段为接口调用的流水号，每次调用接口时都会有一个instId产生。
    private String instId;

    private String attr1;

    private String attr2;

    private String attr3;

}
