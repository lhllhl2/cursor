package com.jasolar.mis.module.system.domain.budget;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PM成员与Owner及成员编码名称联合视图实体
 * 对应视图：DATAINTEGRATION.V_HSP_PM_MEMBER_OWNER_WITH_MEMBER_NAME_CODE
 */
@TableName(value = "DATAINTEGRATION.V_HSP_PM_MEMBER_OWNER_WITH_MEMBER_NAME_CODE", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HspPmMemberOwnerWithMemberNameCodeView {

    @TableField("PM_MEMBER_ID")
    private String pmMemberId;

    @TableField("PM_OWNER_ID")
    private String pmOwnerId;

    @TableField("PRIMARY_MEMBER_ID")
    private String primaryMemberId;

    @TableField("MEMBER_CD")
    private String memberCd;

    @TableField("MEMBER_NM")
    private String memberNm;
}
