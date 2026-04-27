package com.jasolar.mis.module.bpm.api.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 业务数据DTO
 * 
 * @author galuo
 * @date 2025-05-08 15:56
 *
 */
@SuppressWarnings("serial")
@Data
public class BizDTO implements Serializable {

    /** 业务类型 */
    @NotBlank
    @Size(max = 30)
    protected String bizType;

    /** 业务ID */
    @NotNull
    protected Long bizId;

    /** 业务单号, 回写入流程的businessKey */
    @NotBlank
    @Size(max = 30)
    protected String bizNo;
}
