package com.jasolar.mis.framework.data.core;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jasolar.mis.framework.common.enums.CommonStatusEnum;
import com.jasolar.mis.framework.i18n.I18nUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 字典数据
 * 
 * @author galuo
 * @date 2025-03-28 13:45
 *
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("serial")
public class DictData implements Serializable, Comparable<DictData> {
    /** 字典排序 */
    private int sort;
    /** 字典标签 */
    private String label;
    /** 字典值 */
    private String value;

    /** 字典分类 */
    private String dictType;
    /** 上级字典值 */
    private String parentValue;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /** 扩展字段1 */
    private String attr1;

    /** 扩展字段2 */
    private String attr2;

    /** 扩展字段3 */
    private String attr3;

    @Override
    public int compareTo(DictData o) {
        // 将无效状态放到后面
        int r = Integer.compare(CommonStatusEnum.valueOf(status).ordinal(), CommonStatusEnum.valueOf(o.status).ordinal());
        if (r != 0) {
            return r;
        }
        return Comparator.comparing(DictData::getSort).thenComparing((a, b) -> StringUtils.compare(a.getValue(), b.getValue()))
                .compare(this, o);
    }

    /**
     * 字典的国际化标签
     * 
     * @return 国际化文本
     */
    @JsonIgnore
    public String getI18nLabel() {
        // 字典的国际化
        String i18nCode = I18nUtils.DICT_PREFIX + I18nUtils.joinKey(dictType, value);
        return I18nUtils.getMessage(i18nCode, null, label);
    }
}
