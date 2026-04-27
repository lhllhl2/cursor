package com.jasolar.mis.module.system.domain.admin.i18n;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.Map;

/**
 * 国际化菜单翻译 DO
 * 
 * 用于存储菜单的多语言翻译配置，支持不同语言版本的菜单标题和界面元素翻译
 *
 * @author jasolar
 */
@TableName("system_i18n_menu")
@KeySequence("system_i18n_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemI18nMenuDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 菜单主键ID
     */
    private Long menuId;
    
    /**
     * 菜单标题
     */
    private String title;
    
    /**
     * 语言类型：zh-CN, en-US, zh-TW等
     */
    private String locale;
    
    /**
     * JSON格式翻译数据，如：{"button.add": "新增", "button.edit": "编辑", "operation": "操作"}
     */
    private String jsonData;
    
    /**
     * 翻译数据Map（用于业务处理，不映射到数据库）
     */
    private transient Map<String, String> jsonDataMap;
} 