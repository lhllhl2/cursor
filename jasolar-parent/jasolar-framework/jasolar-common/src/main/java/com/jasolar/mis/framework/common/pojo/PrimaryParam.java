package com.jasolar.mis.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 9:04
 * Version : 1.0
 */
@Schema(description = "主键参数")
@Data
public class PrimaryParam {


    @Schema(description = "主键")
    private Long id;
}
