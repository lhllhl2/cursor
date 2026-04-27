package com.jasolar.mis.module.system.controller.admin.user.vo.user;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.jasolar.mis.framework.excel.core.annotations.DictFormat;
import com.jasolar.mis.framework.excel.core.convert.DictConvert;
import com.jasolar.mis.module.system.constant.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - 用户信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("用户编号")
    private Long id;

    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "jasolar")
    @ExcelProperty("用户名称")
    private String username;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xshe")
    @ExcelProperty("用户昵称")
    private String nickname;

    @Schema(description = "备注", example = "我是一个用户")
    private String remark;

    @Schema(description = "部门ID", example = "我是一个用户")
    private Long deptId;

    @Schema(description = "部门CODE", example = "11")
    @ExcelProperty("部门CODE")
    private String deptCode;

    @Schema(description = "部门名称", example = "IT 部")
    @ExcelProperty("部门名称")
    private String deptName;
    
    @Schema(description = "拼音;显示名称的拼音")
    private String pinyin;

    @Schema(description = "英文名称", example = "xshe")
    private String englishName;

    @Schema(description = "法人编码")
    private String legalCode;

    @Schema(description = "法人名称;冗余字段", example = "赵六")
    private String legalName;

    @Schema(description = "岗位编号数组", example = "1")
    private Set<Long> postIds;

    @Schema(description = "用户邮箱", example = "")
    @ExcelProperty("用户邮箱")
    private String email;

    @Schema(description = "手机号码", example = "15601691300")
    @ExcelProperty("手机号码")
    private String mobile;

    @Schema(description = "用户性别，参见 SexEnum 枚举类", example = "1")
    @ExcelProperty(value = "用户性别", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_SEX)
    private Integer sex;

    @Schema(description = "用户头像", example = "http://www.roselife.com/xxx.png")
    private String avatar;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "帐号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "最后登录 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.1.1")
    @ExcelProperty("最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    @ExcelProperty("最后登录时间")
    private LocalDateTime loginDate;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    private LocalDateTime createTime;

    @Schema(description = "事业群CODE")
    private String businessGroupCode;

    @Schema(description = "事业群名称")
    private String businessGroupName;

    @Schema(description = "事业处CODE")
    private String businessUnitCode;

    @Schema(description = "事业处名称")
    private String businessUnitName;

}
