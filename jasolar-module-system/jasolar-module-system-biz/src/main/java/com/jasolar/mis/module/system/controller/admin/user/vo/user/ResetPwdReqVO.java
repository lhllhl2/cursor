package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "管理后台 - 重置用户密码")
@Data
public class ResetPwdReqVO {

    @Schema(description = "用户id")
    private List<Long> userIds;

}
