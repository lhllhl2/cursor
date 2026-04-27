package com.jasolar.mis.module.system.api.org.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrgRespVO {

    private Long id;

    private String name;

    private String code;

    private String pCode;

    @JsonProperty("pName")
    private String pName;

    private String orgType;

    private Boolean isApprovalLastLvl;

    private String scriptType;

    private List<OrgRespVO> children;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String creator;

    private String updater;
}

