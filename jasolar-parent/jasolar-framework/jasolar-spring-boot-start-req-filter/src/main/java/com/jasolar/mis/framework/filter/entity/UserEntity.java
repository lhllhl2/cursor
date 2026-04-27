package com.jasolar.mis.framework.filter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 28/07/2025 9:18
 * Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {

    private Long id;

    private String userName;

    private String displayName;

}
