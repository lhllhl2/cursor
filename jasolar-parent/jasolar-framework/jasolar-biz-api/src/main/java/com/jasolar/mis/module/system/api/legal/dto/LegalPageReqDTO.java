package com.jasolar.mis.module.system.api.legal.dto;

import java.util.List;

import com.jasolar.mis.framework.common.pojo.PageParam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 法人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LegalPageReqDTO extends PageParam {

    private static final long serialVersionUID = 1L;

    @Schema(description = "法人代码集合")
    private List<String> codes;

    @Schema(description = "法人代码;有效的数据必须唯一")
    private String code;

    @Schema(description = "法人名称", example = "王五")
    private String name;

    @Schema(description = "有效状态;0有效，1无效", example = "1")
    private Short status;

}