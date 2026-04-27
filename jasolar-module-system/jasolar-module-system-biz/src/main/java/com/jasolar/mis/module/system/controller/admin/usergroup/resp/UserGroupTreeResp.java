package com.jasolar.mis.module.system.controller.admin.usergroup.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 29/07/2025 11:38
 * Version : 1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupTreeResp {



    private String name;

    private String type;

    private List<UserGroupTreeNodeResp> children;


}
