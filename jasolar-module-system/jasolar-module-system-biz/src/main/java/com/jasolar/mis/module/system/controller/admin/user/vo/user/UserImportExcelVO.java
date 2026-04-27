package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.DictFormat;
import com.jasolar.mis.framework.excel.core.convert.DictConvert;
import com.jasolar.mis.module.system.constant.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class UserImportExcelVO {

    @ExcelProperty("登录名称")
    private String username;

    @ExcelProperty("用户名称")
    private String nickname;

    @ExcelProperty("部门编号")
    private Long deptId;

    @ExcelProperty("用户邮箱")
    private String email;

    @ExcelProperty("手机号码")
    private String mobile;

    @ExcelProperty(value = "用户性别", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_SEX)
    private Integer sex;

    @ExcelProperty("拼音")
    private String pinyin;

    @ExcelProperty("英文名称")
    private String englishName;

    @ExcelProperty("法人编码")
    private String legalCode;

    @ExcelProperty("法人名称")
    private String legalName;

    @ExcelProperty(value = "账号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
