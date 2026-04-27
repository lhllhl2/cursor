package com.jasolar.mis.framework.excel.core.util;

import com.jasolar.mis.framework.common.exception.ErrorCode;

/**
 * 错误码
 * 
 * @author galuo
 * @date 2025-04-07 13:33
 *
 */
public interface ErrorCodes {

    /** 字典下拉解析格式有问题, 必须是:label(value)的格式 */
    ErrorCode DICT_PARSE_FORMAT = new ErrorCode("err.common.excel.dict.parse.format", "下拉字段无法解析, 格式有误:【{0}】");

    /** 字段的数据类型有误无法解析, 如数字字段却在excel中输入了非数字的字符串 */
    ErrorCode CONVERTE_DATA_TYPE = new ErrorCode("err.common.excel.convert.data.type", "\"{1}\"数据错误无法解析");

    /** Excel校验失败抛出的异常 */
    ErrorCode VALID = new ErrorCode("err.common.excel.valid", "第{0}行数据校验失败: {1}");
}
