package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 15:08
 * Version : 1.0
 */
@Schema(description = "管理后台 - 根据关键字查询用户参数")
@Data
public class UserForGroupVo {

    @Schema(description = "关键字")
    private String keyword;

}
