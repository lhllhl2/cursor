package com.jasolar.mis.module.system.api.legal.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 法人 Response VO")
@Data
public class LegalRespDTO {

    @Schema(description = "ID;雪花算法自动生成", requiredMode = Schema.RequiredMode.REQUIRED, example = "26946")
    private Long id;

    @Schema(description = "法人代码;有效的数据必须唯一", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "法人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "有效状态;0有效，1无效", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    /**
     * 财报法人代码
     */
    private String fiCode;
    /**
     * ERP系统
     */
    private String erpType;
    /**
     * SAP法人代碼
     */
    private String sapCode;
    /**
     * 總賬工廠代碼
     */
    private String factoryCode;
    /**
     * 主機
     */
    private String serverName;
    /**
     * 法人本币
     */
    private String currency;
    /**
     * 法人体系
     */
    private String entitySystem;
}