package com.jasolar.mis.module.masterdata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 收货中心 Response VO")
@Data
public class ConsignCenterRespDTO implements Serializable {

    @Schema(description = "ID;雪花算法自动生成", requiredMode = Schema.RequiredMode.REQUIRED, example = "27586")
    private Long id;

    @Schema(description = "收货中心代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "收货中心名称", example = "李四")
    private String name;

    @Schema(description = "厂区")
    private String factoryArea;

    @Schema(description = "HUB仓类型")
    private Boolean hub;

    @Schema(description = "所属部门code")
    private String deptCode;

    @Schema(description = "所属部门名称", example = "王五")
    private String deptName;

    @Schema(description = "法人code")
    private String legalCode;

    @Schema(description = "法人名称", example = "xshe")
    private String legalName;

    @Schema(description = "管理人员账号")
    private String adminNo;

    @Schema(description = "管理人员")
    private String admin;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "是否有效")
    private Boolean effective;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}