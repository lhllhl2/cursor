package com.jasolar.mis.module.infra.api.delay.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DelayMsgDTO implements Serializable {

    private Long bizId;

    private String bizNo;

    private String topic;
}
