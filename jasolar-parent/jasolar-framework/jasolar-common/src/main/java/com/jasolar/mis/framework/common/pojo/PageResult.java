package com.jasolar.mis.framework.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("serial")
@Schema(description = "分页结果")
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PageResult<T> implements Serializable {

    @Schema(description = "数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> list;

    @Schema(description = "总量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long total;

    public PageResult(Long total) {
        this.list = new ArrayList<>();
        this.total = total;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L);
    }

    public static <T> PageResult<T> empty(Long total) {
        return new PageResult<>(total);
    }

    /**
     * 将列表数据转换新的类型
     * 
     * @param <R> 转换后的数据类型
     * @param converter 整个数据转换函数
     * @return 新的分页类型
     */
    public <R> PageResult<R> convert(Function<List<T>, List<R>> converter) {
        if (CollectionUtils.isEmpty(getList())) {
            return new PageResult<R>(Collections.emptyList(), this.getTotal());
        }
        return new PageResult<R>(converter.apply(this.getList()), this.getTotal());
    }

    /**
     * 将列表数据转换新的类型
     * 
     * @param <R> 转换后的数据类型
     * @param converter 单个数据转换函数
     * @return 新的分页类型
     */
    public <R> PageResult<R> map(Function<T, R> converter) {
        if (CollectionUtils.isEmpty(getList())) {
            return new PageResult<R>(Collections.emptyList(), this.getTotal());
        }
        return new PageResult<R>(this.getList().stream().map(converter).toList(), this.getTotal());
    }




}
