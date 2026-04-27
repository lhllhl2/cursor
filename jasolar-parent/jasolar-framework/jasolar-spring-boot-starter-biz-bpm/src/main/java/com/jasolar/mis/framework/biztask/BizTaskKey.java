package com.jasolar.mis.framework.biztask;

import java.io.Serializable;

import com.jasolar.mis.framework.biztask.mq.BizTaskMessage;
import com.jasolar.mis.framework.biztask.mq.BizTaskMessageConverter;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.security.LoginServletUtils;
import com.jasolar.mis.framework.common.security.LoginUser;
import com.jasolar.mis.module.bpm.enums.ModuleEnum;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 业务主键
 * 
 * @author galuo
 * @date 2025-04-11 17:23
 *
 */
@SuppressWarnings("serial")
@Data
public class BizTaskKey implements Serializable {

    /** 所属模块;参见枚举: com.jasolar.mis.module.bpm.enums.ModuleEnum */
    @NotNull
    private ModuleEnum module;

    /** 字典:common_biz_task_type。待办类型 */
    @NotBlank
    private String type;
    /** 字典:bpm_biz_type。业务类型;业务类型, 与流程中的业务类型一致 */
    @NotBlank
    private String bizType;
    /** 业务ID */
    @NotNull
    private Long bizId;
    /** 业务单号 */
    @NotBlank
    private String bizNo;

    /** 用户编号 */
    @NotBlank
    private String userNo;

    /**
     * 用户类型;0.采购平台用户, 1供应商用户
     * 
     * @see UserTypeEnum
     */
    @NotNull
    private UserTypeEnum userType;

    /**
     * 构建业务主键. module使用当前服务名
     * 
     * @param type 待办类型
     * @param bizType 业务数据类型
     * @param bizId 业务ID
     * @param bizNo 业务数据单号
     * @param userType 人员类型
     * @param userNo 人员账号
     * @return
     */
    public static BizTaskKey of(String type, String bizType, Long bizId, String bizNo, UserTypeEnum userType, String userNo) {
        BizTaskKey key = new BizTaskKey();
        key.module = ModuleEnum.getByName(SpringUtil.getApplicationName());
        key.type = type;
        key.bizType = bizType;
        key.bizId = bizId;
        key.bizNo = bizNo;
        key.userType = userType;
        key.userNo = userNo;
        return key;
    }

    /**
     * 构建业务主键, module使用当前服务名, 用户为当前登录用户
     * 
     * @param type 待办类型
     * @param bizType 业务数据类型
     * @param bizId 业务ID
     * @param bizNo 业务数据单号
     * @return
     */
    public static BizTaskKey of(String type, String bizType, Long bizId, String bizNo) {
        BizTaskKey key = new BizTaskKey();
        key.module = ModuleEnum.getByName(SpringUtil.getApplicationName());
        key.type = type;
        key.bizType = bizType;
        key.bizId = bizId;
        key.bizNo = bizNo;

        LoginUser user = LoginServletUtils.getLoginUser();
        key.userType = user.userType();
        key.userNo = user.getNo();
        return key;
    }

    /**
     * 转换为MQ消息
     * 
     * @return MQ消息
     */
    public BizTaskMessage toMessage() {
        return BizTaskMessageConverter.INSTANCE.convert(this);
    }

}
