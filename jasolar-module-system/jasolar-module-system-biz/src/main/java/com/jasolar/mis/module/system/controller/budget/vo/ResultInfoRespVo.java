package com.jasolar.mis.module.system.controller.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Description: 结果信息响应VO
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResultInfoRespVo {

    /**
     * 单据号
     */
    private String documentNo;

    /**
     * 校验结果
     * 参考值：0 (表示通过)
     */
    private String validationResult;

    /**
     * 校验消息
     * 参考值：通过
     */
    private String validationMessage;

    /**
     * 处理时间
     * 参考值：2025-01-01 12:00:00
     */
    private String processTime;

    /**
     * 明细列表
     */
    private List<DetailDetailVo> details;
}

