package com.jasolar.mis.module.masterdata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 料号价格信息
 * 
 * @author galuo
 * @date 2025-04-09 09:23
 *
 */
@SuppressWarnings("serial")
@Schema(description = "料号及价格信息")
@Data
public class MaterialPriceDTO extends MaterialDTO {

    // ======================价格================================

    @Schema(description = "是否锁定;1被锁定，不可进行后续的采购加交易, 0未锁定", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean locked;

    @Schema(description = "区域;字典region", requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;

    @Schema(description = "供应商账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String supplierNo;

    @Schema(description = "供应商准入后的编码;供应商准入后生成的供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String supplierName;

    @Schema(description = "参考交期（天）")
    private Integer deliveryDays;

    @Schema(description = "使用期限(月);根据最近一次核价的【价格有效期】进行计算")
    private BigDecimal duration;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;

    @Schema(description = "单价(不含税)", requiredMode = Schema.RequiredMode.REQUIRED, example = "18845")
    private BigDecimal price;

    @Schema(description = "含税价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceWithTax;

    @Schema(description = "税率;不含税则设为0,1表示100%", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal taxRate;

    @Schema(description = "价格来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String sourcingType;

    @Schema(description = "来源单号;询价单/决标单号")
    private String sourcingNo;


    @Schema(description = "有效期起;入库时计算的实际有效期,保证有效的价格日期不会重合", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate effectiveDate;

    @Schema(description = "有效期止;入库时计算的实际有效期,保证有效的价格日期不会重合", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expirationDate;







}