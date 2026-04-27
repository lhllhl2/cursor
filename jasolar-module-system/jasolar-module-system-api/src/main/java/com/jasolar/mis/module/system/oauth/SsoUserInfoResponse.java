package com.jasolar.mis.module.system.oauth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class SsoUserInfoResponse {
    private String sub;

    @JsonFormat(pattern = "ou_id")
    private String ouId;

    private String nickname;

    @JsonFormat(pattern = "phone_number")
    private String phoneNumber;

    @JsonFormat(pattern = "ou_name")
    private String ouName;

    private String email;

    private String username;
}
