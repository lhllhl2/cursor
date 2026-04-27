package com.jasolar.mis.module.system.api.legal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 法人新增/修改 Request VO")
@Data
public class LegalSaveReqDTO {

    @Schema(description = "ID;雪花算法自动生成。修改时必传", example = "26946")
    private Long id;

    @Schema(description = "法人代码;有效的数据必须唯一", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "法人代码不能为空")
    private String code;

    @Schema(description = "法人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotBlank(message = "法人名称不能为空")
    private String name;

    @Schema(description = "有效状态;0有效，1无效", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "有效状态;0有效，1无效不能为空")
    private Integer status;


    @Schema(description = "国别代码")
    private String countryCode ;
    @Schema(description = "国别名称")
    private String countryName ;

    /**
     * 联系方式
     */
    private String contactInformation;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户银行名称
     */
    private String bankName;
}