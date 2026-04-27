package com.jasolar.mis.framework.common.redis;

/**
 * 将Redis缓存的Key常量抽取的common包，方便其他地方依赖使用<br>
 * 这样不仅仅是system工程使用,其他的包也能使用，方便集中管理
 *
 * @author zhaohuang
 */
public interface RedisKeyConstants {

    default String getKey(String key) {
        return this.getClass().getSimpleName() + ":" + key;
    }

    /**
     * 指定部门的所有子部门编号数组的缓存
     * <p>
     * KEY 格式：dept_children_ids:{id}
     * VALUE 数据类型：String 子部门编号集合
     */
    String DEPT_CHILDREN_ID_LIST = "dept_children_ids";

    /**
     * 角色的缓存
     * <p>
     * KEY 格式：role:{id}
     * VALUE 数据类型：String 角色信息
     */
    String ROLE = "role";

    /**
     * 用户拥有的角色编号的缓存
     * <p>
     * KEY 格式：user_role_ids:{userId}
     * VALUE 数据类型：String 角色编号集合
     */
    String USER_ROLE_ID_LIST = "PERMISSION:ROLE:IDS:USER:ID";

    /**
     * 拥有指定菜单的角色编号的缓存
     * <p>
     * KEY 格式：user_role_ids:{menuId}
     * VALUE 数据类型：String 角色编号集合
     */
    String MENU_ROLE_ID_LIST = "PERMISSION:ROLE:IDS:MENU:ID";

    /**
     * 角色-url权限的缓存
     * <p>
     * KEY 格式：role_permission_urls:{roleId}
     * VALUE 数据格式：String method:url 拼接内容集合
     */
    String ROLE_PERMISSION_URLS = "PERMISSION:URLS:ROLE:ID";

    /**
     * 拥有权限对应的菜单编号数组的缓存
     * <p>
     * KEY 格式：permission_menu_ids:{permission}
     * VALUE 数据类型：String 菜单编号数组
     */
    String PERMISSION_MENU_ID_LIST = "permission_menu_ids";

    /**
     * OAuth2 客户端的缓存
     * <p>
     * KEY 格式：oauth_client:{id}
     * VALUE 数据类型：String 客户端信息
     */
    String OAUTH_CLIENT = "oauth_client";

    /**
     * OAuth2 密钥的缓存
     * <p>
     * KEY 格式：oauth_client:{id}
     * VALUE 数据类型：String 客户端信息
     */
    String OAUTH_CLIENT_SECRET = "oauth_client_secret";

    /**
     * 访问令牌的缓存
     * <p>
     * KEY 格式：oauth2_access_token:{token}
     * VALUE 数据类型：String 访问令牌信息
     * <p>
     * 由于动态过期时间，使用 RedisTemplate 操作
     */
    String OAUTH2_ACCESS_TOKEN = "oauth2_access_token:%s";

    /**
     * 站内信模版的缓存
     * <p>
     * KEY 格式：notify_template:{code}
     * VALUE 数据格式：String 模版信息
     */
    String NOTIFY_TEMPLATE = "notify_template";

    /**
     * 邮件账号的缓存
     * <p>
     * KEY 格式：mail_account:{id}
     * VALUE 数据格式：String 账号信息
     */
    String MAIL_ACCOUNT = "mail_account";

    /**
     * 邮件模版的缓存
     * <p>
     * KEY 格式：mail_template:{code}
     * VALUE 数据格式：String 模版信息
     */
    String MAIL_TEMPLATE = "mail_template";

    /**
     * 短信模版的缓存
     * <p>
     * KEY 格式：sms_template:{id}
     * VALUE 数据格式：String 模版信息
     */
    String SMS_TEMPLATE = "sms_template";

    /**
     * 小程序订阅模版的缓存
     * <p>
     * KEY 格式：wxa_subscribe_template:{userType}
     * VALUE 数据格式 String, 模版信息
     */
    String WXA_SUBSCRIBE_TEMPLATE = "wxa_subscribe_template";

    /**
     * 加一个兜底策略，可以使得所有的url都能访问到，避免重启服务器
     */
    String ALL_URLS_PASS = "PASS:ALL:URLS";

    /**
     * EHR组织管理分页查询的缓存
     * <p>
     * KEY 格式：ehr_org_manage_r:searchPage:{year}:{ehrOrgKey}:{manageOrgKey}:{pageNo}:{pageSize}
     * VALUE 数据类型：PageResult<EhrOrgManageR> 分页查询结果
     * 过期时间：1小时（在application配置中设置）
     */
    String EHR_ORG_MANAGE_R_SEARCH_PAGE = "ehr_org_manage_r:searchPage";

}
