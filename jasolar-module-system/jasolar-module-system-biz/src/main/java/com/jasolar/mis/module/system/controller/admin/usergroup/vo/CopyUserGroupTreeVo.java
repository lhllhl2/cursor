package com.jasolar.mis.module.system.controller.admin.usergroup.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 15:36
 * Version : 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyUserGroupTreeVo {

    private Long fromUserId;
    private List<Long> toUserIds;

}
