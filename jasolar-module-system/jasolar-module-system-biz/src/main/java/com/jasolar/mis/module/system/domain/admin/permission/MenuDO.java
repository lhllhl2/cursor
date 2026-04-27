package com.jasolar.mis.module.system.domain.admin.permission;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 菜单 DO
 *
 * @author ruoyi
 */
@TableName(value = "system_menu", autoResultMap = true)
@KeySequence("system_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDO extends BaseDO {

    /**
     * 根节点编号
     */
    public static final Long ID_ROOT = 0L;

    /**
     * 主键ID
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 菜单类型：catalog目录,menu菜单,embedded内嵌,link外链,button按钮
     */
    private String type;

    /**
     * 父级菜单ID，顶级菜单为0
     */
    private Long pid;

    /**
     * 后端权限标识
     * 一般格式为：${系统}:${模块}:${操作}
     * 例如说：system:admin:add，即 system 服务的添加管理员。
     *
     * 当我们把该 MenuDO 赋予给角色后，意味着该角色有该资源：
     * - 对于后端，配合 @PreAuthorize 注解，配置 API 接口需要该权限，从而对 API 接口进行权限控制。
     * - 对于前端，配合前端标签，配置按钮是否展示，避免用户没有该权限时，结果可以看到该操作。
     */
    private String authCode;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 激活时显示的图标
     */
    private String activeIcon;

    /**
     * 状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 菜单排序
     */
    private Integer menuOrder;

    /**
     * 需要激活的菜单路径
     */
    private String activePath;

    /**
     * 是否固定标签页
     */
    private Integer affixTab;

    /**
     * 固定标签页顺序
     */
    private Integer affixTabOrder;

    /**
     * 徽标内容
     */
    private String badge;

    /**
     * 徽标类型：dot小红点,normal文字
     */
    private String badgeType;

    /**
     * 徽标变体
     */
    private String badgeVariants;

    /**
     * 是否在菜单中隐藏子菜单
     */
    private Integer hideChildrenInMenu;

    /**
     * 是否在面包屑中隐藏
     */
    private Integer hideInBreadcrumb;

    /**
     * 是否在菜单中隐藏
     */
    private Integer hideInMenu;

    /**
     * 是否在标签页中隐藏
     */
    private Integer hideInTab;

    /**
     * iframe 源地址
     */
    private String iframeSrc;

    /**
     * 是否缓存
     */
    private Integer keepAlive;

    /**
     * 链接地址
     */
    private String link;

    /**
     * 最大打开标签页数量
     */
    private Integer maxNumOfOpenTab;

    /**
     * 是否不使用基础布局
     */
    private Integer noBasicLayout;

    /**
     * 是否在新窗口打开
     */
    private Integer openInNewWindow;

    /**
     * 查询参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> queryParams;

    /**
     * 菜单编码
     */
    private String code;
}
