package com.jasolar.mis.module.system.controller.ehr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ERP视图中未映射的科目")
public class ErpUnmappedAccountVO {

    @Schema(description = "ERP科目编码(MEMBERCODE)")
    private String memberCode;

    @Schema(description = "ERP科目名称(MEMBERNAME)")
    private String memberName;
}
