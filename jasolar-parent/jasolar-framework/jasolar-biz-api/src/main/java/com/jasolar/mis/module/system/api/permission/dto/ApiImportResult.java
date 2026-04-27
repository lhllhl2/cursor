package com.jasolar.mis.module.system.api.permission.dto;

import lombok.Data;

/**
 * @author zhahuang
 */
@Data
public class ApiImportResult {
    public ApiImportResult() {
        this.successCount = 0;
        this.failureCount = 0;
        this.totalCount = 0;
        this.existCount = 0;
    }

    private int successCount;
    private int failureCount;
    private int totalCount;
    private int existCount;


    @Override
    public String toString() {
        return "========API导入结果：总共数量[" +
                totalCount +
                "]条数据，成功导入[" +
                successCount +
                "]条数据，失败[" +
                failureCount +
                "]条数据，已存在[" +
                existCount +
                "]条数据==========";
    }
}
