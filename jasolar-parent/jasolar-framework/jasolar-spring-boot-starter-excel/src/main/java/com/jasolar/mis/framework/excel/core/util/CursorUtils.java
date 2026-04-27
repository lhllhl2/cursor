package com.jasolar.mis.framework.excel.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.ibatis.cursor.Cursor;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 游标查询导出数据. 使用游标查询结果可以防止导出的数据量太大造成内存溢出
 * 
 * @author galuo
 * @date 2025-03-31 14:29
 *
 */
@Slf4j
public class CursorUtils {

    /** 游标每批次处理的数量 */
    public static final int BATCH_SIZE = 5000;

    /**
     * 
     * 将查询到的游标数据进行导出
     * 
     * @param <DTO> 导出的数据类型
     * @param <DO> 查询到的数据库实体DO类型
     * 
     * @param response HttpServletResponse
     * @param filename 导出的文件名,不包含时间戳和后缀名.导出的文件名会添加{@link ExcelTypeEnum#XLSX}做后缀
     * @param headClass 数据的类,字段中会添加导出的相关注解{@link ExcelProperty}
     * @param cursor 查询处的游标数据
     * @param converter 数据转换函数, 将DO转换为DTO
     * @throws IOException
     */
    public static <DTO, DO> void export(HttpServletResponse response, String filename, Class<DTO> headClass, Cursor<DO> cursor,
            Function<DO, DTO> converter) throws IOException {
        log.info("开始导出数据");
        long ms = System.currentTimeMillis();
        ExcelWriterBuilder builder = ExcelUtils.builder(response, filename, headClass);
        List<DTO> buffer = new ArrayList<>(BATCH_SIZE);
        AtomicLong count = new AtomicLong();

        WriteSheet writeSheet = EasyExcelFactory.writerSheet().build();
        ExcelWriter writer = builder.build();
        cursor.forEach(e -> {
            buffer.add(converter.apply(e));
            if (buffer.size() >= BATCH_SIZE) {
                count.addAndGet(buffer.size());
                log.info("写入数据: {} 条, 已处理: {} 条", buffer.size(), count);
                writer.write(buffer, writeSheet);
                buffer.clear();
            }
        });

        if (!buffer.isEmpty()) {
            count.addAndGet(buffer.size());
            writer.write(buffer, writeSheet);
        } else if (count.get() < 1) {
            // 没有任何数据, 必须调用一次fill，否则导出的文件可能是原始模板文件
            // writer.write(buffer, writeSheet);
        }
        log.info("导出完成, 数据合计: {} 条, 耗时:{}ms", count, System.currentTimeMillis() - ms);

        writer.finish();
    }
}
