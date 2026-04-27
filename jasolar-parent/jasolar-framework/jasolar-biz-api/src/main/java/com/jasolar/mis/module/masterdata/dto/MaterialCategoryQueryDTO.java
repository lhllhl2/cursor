package com.jasolar.mis.module.masterdata.dto;

import java.io.Serializable;
import java.util.List;

import com.jasolar.mis.framework.common.pojo.PageParam;

import lombok.Builder;
import lombok.Data;

/**
 * 物料分类的查询条件
 * 
 * @author galuo
 * @date 2025-04-08 21:24
 *
 */
@SuppressWarnings("serial")
@Data
@Builder
public class MaterialCategoryQueryDTO extends PageParam implements Serializable {

    /** 查询的阶别;1一阶，2二阶，3三阶，4四阶（也即品名） */
    private List<Integer> lvs;

    /** 根据分类编码查询,一般用在查询4阶品名 */
    private List<String> codes;

    /** 根据1阶分类编码查询 */
    private List<String> lv1Codes;
    /** 根据2阶分类编码查询 */
    private List<String> lv2Codes;
    /** 根据3阶分类编码查询 */
    private List<String> lv3Codes;

    /* 以下3个单个查询条件用于兼容前端历史代码 */
    /** 根据1阶分类编码查询 */
    private String lv1Code;
    /** 根据2阶分类编码查询 */
    private String lv2Code;
    /** 根据3阶分类编码查询 */
    private String lv3Code;

    /** 状态,下拉选择时, 一般仅查询有效的数据 */
    private Integer status;

    /** 通过名称模糊查询,不区分大小写 */
    private String name;

    /** 料号属性;字典material_nature. */
    private List<String> materialNatures;

    /** 通过名称或者code模糊查询,不区分大小写 */
    private String codeOrName;

    //============增加简体和繁体===========
    private String simplifiedName;

    private String traditionalName;

    private String simplifiedCodeOrName;

    private String traditionalCodeOrName;

}
