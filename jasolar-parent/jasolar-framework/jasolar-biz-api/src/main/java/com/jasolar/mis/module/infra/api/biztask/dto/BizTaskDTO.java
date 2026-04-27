package com.jasolar.mis.module.infra.api.biztask.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.jasolar.mis.framework.common.enums.UserTypeEnum;
import com.jasolar.mis.framework.common.validation.group.Submit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 接口使用的DTO
 * 
 * @author galuo
 * @date 2025-04-11 18:16
 *
 */
@SuppressWarnings("serial")
@Data
public class BizTaskDTO extends BizTaskKeyDTO implements Serializable {

    /** 用户名称 */
    @Size(max = 100)
    private String userName;

    /**
     * 待办发送用户类型;0.采购平台用户, 1供应商用户
     * 
     * @see UserTypeEnum
     */
    private Integer senderType;

    /** 待办发送用户账号 */
    @Size(max = 30)
    private String senderNo;

    /** 待办发送用户名称 */
    @Size(max = 100)
    private String senderName;

    /** 待办标题 */
    @NotBlank(groups = Submit.class)
    @Size(max = 100)
    private String title;
    /**
     * @see com.fiifoxconn.mis.framework.biztask.BizTaskStatusEnum
     */
    @Size(max = 30)
    private String status;
    /**
     * 提交日期;如果是草稿,等于创建时间.提交审批后,则为提交审批的时间
     */
    private LocalDateTime submitTime;
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /** 待办跳转地址 */
    @NotBlank(groups = Submit.class)
    @Size(max = 200)
    private String url;
    /** 内容 */
    private String content;

    /** 扩展字段 */
    private Map<String, Object> attrs;

    /** 读取扩展字段 */
    public Object getAttr(String key) {
        return attrs == null ? null : attrs.get(key);
    }

    /**
     * 注入扩展字段
     * 
     * @param key 字段名
     * @param value 字段值
     */
    public void setAttr(String key, Object value) {
        if (attrs == null) {
            attrs = new HashMap<>();
        }
        attrs.put(key, value);
    }

    /**
     * 注入所有扩展字段
     * 
     * @param attrs 所有扩展字段
     */
    public void putAttrs(Map<String, Object> attrs) {
        if (this.attrs == null) {
            this.attrs = new HashMap<>();
        }
        this.attrs.putAll(attrs);
    }
}
