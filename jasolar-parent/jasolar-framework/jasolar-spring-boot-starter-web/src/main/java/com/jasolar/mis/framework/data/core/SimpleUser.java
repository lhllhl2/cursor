package com.jasolar.mis.framework.data.core;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 
 * @author galuo
 * @date 2025-05-19 18:14
 *
 */
@SuppressWarnings("serial")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUser implements Serializable {

    /** 人员ID */
    private Long id;

    /** 人员工号 */
    private String userNo;

    /** 人员姓名 */
    private String userName;
}
