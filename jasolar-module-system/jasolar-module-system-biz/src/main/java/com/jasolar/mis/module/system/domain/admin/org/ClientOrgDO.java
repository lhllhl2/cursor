package com.jasolar.mis.module.system.domain.admin.org;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 客商组织 DO
 * 对应SYSTEM_ORIGINAL_ORGANIZATION表
 * 注意：此表没有逻辑删除字段，因此不继承BaseDO
 *
 * @author jasolar
 */
@TableName(value = "SYSTEM_ORIGINAL_ORGANIZATION", autoResultMap = true)
@Data
public class ClientOrgDO {

    /**
     * LEVEL1
     */
    private String level1;

    /**
     * LEVEL2
     */
    private String level2;

    /**
     * LEVEL3
     */
    private String level3;

    /**
     * LEVEL4
     */
    private String level4;

    /**
     * LEVEL5
     */
    private String level5;

    /**
     * 父级组织编码
     */
    private String orgParentCd;

    /**
     * 组织编码
     */
    private String orgCd;

    /**
     * 组织描述（中文）
     */
    private String orgDescCn;

    /**
     * 组织描述（英文）
     */
    private String orgDescEn;

    /**
     * 年
     */
    private String year;

    /**
     * 月
     */
    private String period;

    /**
     * 是否末级
     */
    private String isLastLvl;

    /**
     * 本位币
     */
    private String currencyLc;

    /**
     * 统一简称
     */
    private String usn;

    /**
     * 社会信用代码
     */
    private String scc;

    /**
     * 注册号
     */
    private String regNo;

    /**
     * 是否合并报表范围内
     */
    private String isConsol;

    /**
     * 是否为外部关联方
     */
    private String isExtRel;

    /**
     * 外部关联方类型
     */
    private String extRelTyp;

    /**
     * 国家/地区
     */
    private String country;

    /**
     * 是否为境外主体
     */
    private String isOffshore;

    /**
     * 收购时间
     */
    private String acqDate;

    /**
     * 成立时间
     */
    private String estDate;

    /**
     * 注销时间
     */
    private String cancelDate;

    /**
     * 出售时间
     */
    private String dispDate;

    /**
     * 产业群分类
     */
    private String indClust;

    /**
     * 所属事业部
     */
    private String bizDiv;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 上报时间
     */
    private String rptDate;

    /**
     * 注册地址
     */
    private String regAddr;

    /**
     * 常驻办公地址
     */
    private String bizAddr;

    /**
     * 注册金额
     */
    private String regCap;

    /**
     * 持股比例
     */
    private String sharePct;

    /**
     * 主营业务
     */
    private String mainBiz;

    /**
     * 并入合并报表方式
     */
    private String consolMeth;

    /**
     * 报表语言
     */
    private String reportLanguage;

    /**
     * 处理时间
     */
    private String etlTime;

}
