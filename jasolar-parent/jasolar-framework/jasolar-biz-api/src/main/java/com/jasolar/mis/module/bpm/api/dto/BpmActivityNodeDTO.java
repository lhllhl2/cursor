package com.jasolar.mis.module.bpm.api.dto;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 流程节点信息
 * 
 * @author galuo
 * @date 2025-06-09 15:02
 *
 */
@SuppressWarnings("serial")
@Data
public class BpmActivityNodeDTO implements Serializable {

    @Schema(description = "节点编号")
    private String id;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "计算出的所有候选人用户列表")
    private List<UserDTO> candidateUsers;

    @Schema(description = "用户精简信息 VO")
    @Data
    public static class UserDTO {
        @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;
        @Schema(description = "登录账号")
        private String username;
        @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
        private String nickname;
        @Schema(description = "用户头像")
        private String avatar;

        @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long deptId;
        @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部")
        private String deptName;

    }
}
