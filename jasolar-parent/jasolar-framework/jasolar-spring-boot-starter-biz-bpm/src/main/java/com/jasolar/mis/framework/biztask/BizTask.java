package com.jasolar.mis.framework.biztask;

import java.util.HashMap;
import java.util.Map;

import com.jasolar.mis.framework.biztask.mq.BizTaskMessage;
import com.jasolar.mis.framework.biztask.mq.BizTaskMessageConverter;
import com.jasolar.mis.framework.common.enums.UserTypeEnum;

import lombok.Data;

/**
 * 待办任务
 * 
 * @author galuo
 * @date 2025-04-11 17:31
 *
 */
@SuppressWarnings("serial")
@Data
public class BizTask extends BizTaskKey {

    /** 用户名称 */
    private String userName;

    /**
     * 待办发送用户类型;0.采购平台用户, 1供应商用户
     * 
     * @see UserTypeEnum
     */
    private UserTypeEnum senderType;

    /** 待办发送用户账号 */
    private String senderNo;

    /** 待办发送用户名称 */
    private String senderName;

    /** 待办标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 待办跳转地址;WEB端页面点击待办时打开的相对路径, 不应该包含域名和跟. 默认会使用字典配置读取后替换. 一般不需要业务系统调用的时候传入 */
    private String url;

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

    /**
     * 构建待办任务
     * 
     * @param key
     * @param title
     * @param content
     * @return
     */
    public static BizTask of(BizTaskKey key, String title, String content) {
        return BizTaskMessageConverter.INSTANCE.convert2Task(key, title, content);
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
