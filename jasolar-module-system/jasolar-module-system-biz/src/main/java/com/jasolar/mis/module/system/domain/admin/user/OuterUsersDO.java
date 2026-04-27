package com.jasolar.mis.module.system.domain.admin.user;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jasolar.mis.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 外部用户信息 DO
 *
 * @author 管理员
 */
@TableName("system_outer_users")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OuterUsersDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 员工ID（员工号)
     */
    private String empId;
    /**
     * 员工名
     */
    private String empName;
    /**
     * 员工拼音
     */
    private String empNamePinyin;
    /**
     * 英文名
     */
    private String empEnglishName;
    /**
     * 性别
     */
    private String gender;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 电话
     */
    private String phone;
    /**
     * 在职离职状态
     */
    private String employmentStatus;
    /**
     * 法人code
     */
    private String entityCode;
    /**
     * 费用代码/部门代码
     */
    private String departCode;
    /**
     * 组织CODE
     */
    private String organizationCode;
    /**
     * 导入状态（0:待导入，1:已导入）
     */
    private Short status;

}