package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import com.jasolar.mis.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户分页查询 Request VO
 *
 * @author DTT
 */
@Schema(description = "管理后台 - 用户分页查询 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPageReqVO extends PageParam {

    @Schema(description = "用户工号，模糊匹配", example = "jasolar")
    private String userName;


    @Schema(description = "姓名，模糊匹配", example = "jasolar")
    private String displayName;


}
