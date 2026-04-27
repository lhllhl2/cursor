package com.jasolar.mis.module.system.api.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "RPC 服务 - 字典数据 Response DTO")
@Data
public class DictDataRespDTO {

    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "fiifoxconn")
    private String label;

    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    private String value;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "sys_common_sex")
    private String dictType;

    @Schema(description = "上级类型", example = "sys_common_sex")
    private String parentDictType;

    @Schema(description = "上级键值", example = "1")
    private String parentValue;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status; // 参见 CommonStatusEnum 枚举

    @Schema(description = "扩展字段1")
    private String attr1;

    @Schema(description = "扩展字段2")
    private String attr2;

    @Schema(description = "扩展字段3")
    private String attr3;

    @Schema(description = "排序")
    private Integer sort;
}
