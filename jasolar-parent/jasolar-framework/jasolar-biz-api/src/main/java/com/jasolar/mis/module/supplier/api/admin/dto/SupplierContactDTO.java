package com.jasolar.mis.module.supplier.api.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>|</pre>
 *
 * <br>JDK 版本：17
 *
 * @author yxiacq
 * @version 1.0
 * @since 2025-04-07
 */

@Data
public class SupplierContactDTO implements Serializable {

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "供应商编号")
    private String supplierNo;

    @Schema(description = "供应商代码")
    private String supplierCode;

    @Schema(description = "联系人信息")
    private List<ContactDTO> contactDTOList;

}
