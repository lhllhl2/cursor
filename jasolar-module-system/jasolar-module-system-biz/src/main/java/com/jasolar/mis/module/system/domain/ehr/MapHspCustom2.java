package com.jasolar.mis.module.system.domain.ehr;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Description: 资产类型映射表实体
 * Author : Auto Generated
 * Date : 2025-01-XX
 * Version : 1.0
 */
@TableName(value = "dataintegration.map_hsp_custom2", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MapHspCustom2 {

    /**
     * 资产值
     */
    private String assetValue;

    /**
     * 资产描述
     */
    private String assetDes;

    /**
     * 弹性值
     */
    private String flexValue;

    /**
     * 描述
     */
    private String description;

    /**
     * 预算费用类型编码2
     */
    private String bgtCust2Cd;

}

