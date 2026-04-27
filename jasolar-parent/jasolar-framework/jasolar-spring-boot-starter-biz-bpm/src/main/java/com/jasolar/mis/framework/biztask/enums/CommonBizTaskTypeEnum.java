package com.jasolar.mis.framework.biztask.enums;

/**
 * 业务任务类型枚举，定义了不同类型的通用业务任务。
 *
 * 该枚举包含了以下几种任务类型：
 * - SOURCING_REQUIREDMENT: 采购需求
 * - SOURCING_INQUIRY_APPROVED: 询价审批通过
 * - SOURCING_INQUIRY_DELAY: 询价延期
 * - SOURCING_INQUIRY_INVITE_AGENT: 邀请代理商参与询价
 */
public enum CommonBizTaskTypeEnum {

    /** 寻源需求 */
    SOURCING_REQUIREDMENT,
    /** 寻源询价-发布 */
    SOURCING_INQUIRY_APPROVED,
    /** 询价-报价延期 */
    SOURCING_INQUIRY_DELAY,
    /** 询价-通知代购厂商报价 */
    SOURCING_INQUIRY_INVITE_AGENT,
    /** 询价-修改规格 */
    SOURCING_INQUIRY_MODIFY_SPEC,
    /** 招标-发布 */
    SOURCING_BIDDING_APPROVED,


    /** 订单-请购单-审批通过 - 通知申请人 */
    ORDER_APPLICATION_APPROVED,
    /** 订单-采购单-审批通过 - 通知申请人 */
    ORDER_PURCHASE_APPROVED,
    /** 订单-采购单-审批通过 - 通知供应商 */
    ORDER_PURCHASE_SUPPLIER,
    /** 订单-采购单-审批通过 - 发送至请购人 */
    ORDER_ACCEPTANCE_APPROVED,
    /** 订单-领用单 */
    ORDER_RECEIPT,
    /**
     *结算供应商对账单
    */
    SETTLEMENT_SUPPLIER_SOA,


    /**
     *料号新增申请单
     */
    MASTER_MATERIAL,

    /**
     * 供应商维护统制科目待办
     */
    SUPPLIER_SUBJECT
    ;



}
