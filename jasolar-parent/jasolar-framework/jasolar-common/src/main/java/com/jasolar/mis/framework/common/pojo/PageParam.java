package com.jasolar.mis.framework.common.pojo;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * 分页参数, 第一页页码为{@link FIRST_PAGE_NO}, 默认每页{@link DEFAULT_PAGE_SIZE}条数据. 每页最大{@link MAX_PAGE_SIZE}.
 * 可设置为{@link PAGE_SIZE_NONE} 表示不分页全量查询, 全量查询主要用于内部feign调用,外部接口调用慎用.
 * 注意不要使用{@link Builder}注解, mapstruct生成的代码会使用Builder生成对象,父类和子类关系的时候会调用父类的builder造成生成的代码有误
 * 
 * @author galuo
 * @date 2025-03-28 10:23
 *
 */
@SuppressWarnings("serial")
@Schema(description = "分页参数")
@Data
public class PageParam implements Serializable {

    /** 第一页页码,从1开始 */
    private static final int FIRST_PAGE_NO = 1;

    /** 每页默认查询10条数据 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大分页条数 */
    public static final int MAX_PAGE_SIZE = 10000;

    /**
     * 每页条数 - 不分页
     * <p>
     * 例如说，导出接口，可以设置 {@link #pageSize} 为 -1 不分页，查询所有数据。
     */
    public static final Integer PAGE_SIZE_NONE = -1;

    @Schema(description = "页码，从 " + FIRST_PAGE_NO + " 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 " + FIRST_PAGE_NO)
    private Integer pageNo = FIRST_PAGE_NO;

    @Schema(description = "每页条数，最大值为:" + MAX_PAGE_SIZE, requiredMode = Schema.RequiredMode.REQUIRED, example = DEFAULT_PAGE_SIZE + "")
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数最大值为 " + MAX_PAGE_SIZE)
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 设置分页大小, 不能超出{@link #MAX_PAGE_SIZE},防止数据太大内存溢出. 超出后直接设置为{@link #MAX_PAGE_SIZE}
     * 
     * @param pageSize 分页大小
     */
    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
    }

    /**
     * 查询的开始行
     * 
     * @return 查询的开始行
     */
    public int firstResult() {
        return (getPageNo() - 1) * getPageSize();
    }

    /**
     * 查询的最大行数, 也即分页数量
     * 
     * @return
     */
    public int maxResults() {
        return getPageSize();
    }
}
