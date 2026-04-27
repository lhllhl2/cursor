package com.jasolar.mis.module.masterdata.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 价格库查询条件
 * 
 * @author galuo
 * @date 2025-04-09 19:37
 *
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialPriceQueryDTO extends MaterialQueryDTO{



    @Schema(description = "区域;字典region")
    private String region;

    @Schema(description = "供应商账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String supplierNo;

    @Schema(description = "供应商准入后的编码;供应商准入后生成的供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String supplierName;

    @Schema(description = "是否锁定")
    private Boolean locked;

}
