package com.jasolar.mis.module.system.domain.admin.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 04/11/2025 13:58
 * Version : 1.0
 */
@TableName("OUT_CLIENT_INFO")
@Data
public class OutClientInfoDo {

    private Long id;

    private String clientName;

    private String clientSecret;

    private String remark;

}
