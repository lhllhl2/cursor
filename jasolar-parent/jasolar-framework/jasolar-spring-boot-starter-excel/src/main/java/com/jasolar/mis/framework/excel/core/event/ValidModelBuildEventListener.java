package com.jasolar.mis.framework.excel.core.event;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ModelBuildEventListener;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import com.alibaba.excel.support.cglib.beans.BeanMap;
import com.alibaba.excel.util.BeanMapUtils;
import com.alibaba.excel.util.ClassUtils;
import com.alibaba.excel.util.ConverterUtils;
import com.jasolar.mis.framework.common.exception.I18nedException;
import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import com.jasolar.mis.framework.excel.core.handler.I18nHeaderWriteHandler;
import com.jasolar.mis.framework.excel.core.util.ErrorCodes;
import com.jasolar.mis.framework.i18n.I18nUtils;

import cn.hutool.core.text.StrPool;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 将Excel数据解析得到对象后, 将解析的对象通过{@link Valid}进行验证
 * 
 * @author galuo
 * @date 2025-04-17 11:00
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ValidModelBuildEventListener<T> extends ModelBuildEventListener {

    /** 每一行消息之间的分隔符 */
    @Setter
    @Getter
    private String separator = ";\n";

    /** 异常消息 */
    private final StringBuilder message = new StringBuilder();

    /** 数据类型,必须与{@link EasyExcel#read}方法指定的一致 */
    private final Class<T> headClass;

    /** 校验分组, 可以为null,则表示不做valid校验 */
    private final Class<?>[] groups;

    /** 验证对象, 一般通过{@code SpringUtils.getBean(Validator.class)} 获取 */
    private final Validator validator;

    /**
     * 校验默认分组
     * 
     * @param <T> 数据类型
     * @param headClass 数据class
     * @return
     */
    public static <T> ValidModelBuildEventListener<T> of(Class<T> headClass) {
        return new ValidModelBuildEventListener<T>(headClass, new Class<?>[] { Default.class }, SpringUtils.getBean(Validator.class));
    }

    /**
     * 指定校验的分组,为null,则表示不做valid校验
     * 
     * @param <T> 数据类型
     * @param headClass 数据class
     * @param groups 校验的分组,可以为null,则表示不做valid校验
     * @return
     */
    public static <T> ValidModelBuildEventListener<T> of(Class<T> headClass, @Nullable Class<?>[] groups) {
        return new ValidModelBuildEventListener<T>(headClass, groups, SpringUtils.getBean(Validator.class));
    }

    /**
     * 指定校验的分组
     * 
     * @param <T> 数据类型
     * @param headClass 数据class
     * @param groups 校验的分组, 可以为null,则表示不做valid校验
     * @param validator 验证对象
     * @return
     */
    public static <T> ValidModelBuildEventListener<T> of(Class<T> headClass, @Nullable Class<?>[] groups, Validator validator) {
        return new ValidModelBuildEventListener<T>(headClass, groups, validator);
    }

    @Override
    public void invoke(Map<Integer, ReadCellData<?>> cellDataMap, AnalysisContext context) {
        ReadSheetHolder readSheetHolder = context.readSheetHolder();
        T data = buildUserModel(cellDataMap, readSheetHolder, context);
        context.readRowHolder().setCurrentRowAnalysisResult(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (this.hasError()) {
            // 全部解析后抛出异常
            throw new I18nedException(getMessage());
        }
    }

    /**
     * 将获取的Excel行数据转换为{@link #headClass}类型的数据
     * 
     * @param cellDataMap
     * @param readSheetHolder
     * @param context
     * @return 解析后的数据
     */
    private T buildUserModel(Map<Integer, ReadCellData<?>> cellDataMap, ReadSheetHolder readSheetHolder, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
        T resultModel;
        try {
            resultModel = (T) BeanUtils.instantiateClass(headClass);
        } catch (Exception e) {
            throw new ExcelDataConvertException(rowIndex, 0, new ReadCellData<>(CellDataTypeEnum.EMPTY), null,
                    "Can not instance class: " + headClass.getName(), e);
        }

        Map<Integer, Head> headMap = readSheetHolder.excelReadHeadProperty().getHeadMap();
        BeanMap dataMap = BeanMapUtils.create(resultModel);

        StringBuilder convertErrorFields = new StringBuilder();
        for (Map.Entry<Integer, Head> entry : headMap.entrySet()) {
            Integer index = entry.getKey();
            if (!cellDataMap.containsKey(index)) {
                continue;
            }
            Head head = entry.getValue();
            String fieldName = head.getFieldName();
            try {
                Object value = ConverterUtils.convertToJavaObject(cellDataMap.get(index), head.getField(),
                        ClassUtils.declaredExcelContentProperty(dataMap, headClass, fieldName, readSheetHolder),
                        readSheetHolder.converterMap(), context, rowIndex, index);
                if (value != null) {
                    dataMap.put(fieldName, value);
                }
            } catch (Exception ex) {
                // 字段解析失败,可能是数据类型有问题, 如数字字段输入了字符串
                log.warn("第" + rowIndex + "行的字段: " + fieldName + " 数据转换失败", ex);
                append(convertErrorFields, StringUtils.join(I18nHeaderWriteHandler.toI18nHeadNames(head.getHeadNameList()), StrPool.SLASH),
                        StrPool.COMMA);
            }
        }

        if (!convertErrorFields.isEmpty()) {
            String msg = I18nUtils.getMessage(ErrorCodes.CONVERTE_DATA_TYPE.getCode(), new Object[] { convertErrorFields });
            this.addMessage(rowIndex, msg);
        } else {
            this.valid(rowIndex, resultModel);
        }

        return resultModel;
    }

    /**
     * 验证解析到的数据对象
     * 
     * @param rowIndex 行号
     * @param data 解析的数据对象
     */
    protected void valid(int rowIndex, T data) {
        if (this.groups == null) {
            return;
        }
        Set<ConstraintViolation<T>> set = validator.validate(data, groups);
        if (set.isEmpty()) {
            return;
        }

        StringBuilder msgs = new StringBuilder();
        for (ConstraintViolation<T> cv : set) {
            if (!msgs.isEmpty()) {
                msgs.append(StrPool.COMMA);
            }
            msgs.append(cv.getMessage());
        }

        if (!msgs.isEmpty()) {
            this.addMessage(rowIndex, msgs.toString());
        }
    }

    /**
     * 添加消息
     * 
     * @param message 原始消息
     * @param str 要增加的消息
     * @param separator 分隔符
     */
    static void append(StringBuilder message, String str, String separator) {
        if (!message.isEmpty()) {
            message.append(separator);
        }
        message.append(str);
    }

    /**
     * 添加异常消息
     * 
     * @param rowIndex 异常的行号
     * @param msg 异常消息
     */
    protected void addMessage(int rowIndex, String msg) {
        msg = I18nUtils.getMessage(ErrorCodes.VALID.getCode(), new Object[] { rowIndex, msg });
        append(message, msg, this.separator);
    }

    /**
     * 获取异常消息
     * 
     * @return 异常消息
     */
    public String getMessage() {
        return this.message.toString();
    }

    /**
     * 是否有异常
     * 
     * @return
     */
    public boolean hasError() {
        return !message.isEmpty();
    }

}
