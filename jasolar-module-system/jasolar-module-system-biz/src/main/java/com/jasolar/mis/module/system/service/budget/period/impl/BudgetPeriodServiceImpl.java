package com.jasolar.mis.module.system.service.budget.period.impl;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.budget.BudgetPeriod;
import com.jasolar.mis.module.system.mapper.budget.BudgetPeriodMapper;
import com.jasolar.mis.module.system.service.budget.period.BudgetPeriodService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 预算期间 Service 实现类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Service
@Slf4j
public class BudgetPeriodServiceImpl implements BudgetPeriodService {

    @Resource
    private BudgetPeriodMapper budgetPeriodMapper;

    @Resource
    private IdentifierGenerator identifierGenerator;

    private static final String PERIOD_TYPE_MONTHLY = "MONTHLY";
    private static final String PERIOD_TYPE_QUARTERLY = "QUARTERLY";
    private static final String PERIOD_TYPE_HALFYEARLY = "HALFYEARLY";
    private static final String PERIOD_TYPE_YEARLY = "YEARLY";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generatePeriodData() {
        log.info("开始生成模拟预算期间数据");

        List<BudgetPeriod> periodList = new ArrayList<>();

        // 生成2024-2026年的数据
        for (int year = 2024; year <= 2026; year++) {
            String yearStr = String.valueOf(year);

            // 1. 生成12条月度数据
            for (int month = 1; month <= 12; month++) {
                BudgetPeriod period = createPeriod(
                        yearStr,
                        String.format("%02d", month),
                        PERIOD_TYPE_MONTHLY,
                        year,
                        month
                );
                periodList.add(period);
            }

            // 2. 生成4条季度数据
            // Q1: 1-3月，month字段值为01
            periodList.add(createQuarterlyPeriod(yearStr, "01", year, 1, 3));
            // Q2: 4-6月，month字段值为04
            periodList.add(createQuarterlyPeriod(yearStr, "04", year, 4, 6));
            // Q3: 7-9月，month字段值为07
            periodList.add(createQuarterlyPeriod(yearStr, "07", year, 7, 9));
            // Q4: 10-12月，month字段值为10
            periodList.add(createQuarterlyPeriod(yearStr, "10", year, 10, 12));

            // 3. 生成2条半年度数据
            // H1: 1-6月，month字段值为01
            periodList.add(createHalfYearlyPeriod(yearStr, "01", year, 1, 6));
            // H2: 7-12月，month字段值为07
            periodList.add(createHalfYearlyPeriod(yearStr, "07", year, 7, 12));

            // 4. 生成1条年度数据
            // 年度：1-12月，month字段值为01
            periodList.add(createYearlyPeriod(yearStr, year));
        }

        // 批量插入
        boolean success = budgetPeriodMapper.insertBatch(periodList);
        int successCount = success ? periodList.size() : 0;

        String result = String.format("生成模拟预算期间数据完成！共生成 %d 条数据，成功插入 %d 条", periodList.size(), successCount);
        log.info(result);
        return result;
    }

    /**
     * 创建月度期间
     */
    private BudgetPeriod createPeriod(String year, String month, String periodType, int yearInt, int monthInt) {
        BudgetPeriod period = new BudgetPeriod();
        period.setId(identifierGenerator.nextId(null).longValue());
        period.setYear(year);
        period.setMonth(month);
        period.setPeriodType(periodType);

        // 计算开始日期和结束日期
        YearMonth yearMonth = YearMonth.of(yearInt, monthInt);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        period.setStartDate(startDate.atStartOfDay());
        period.setEndDate(endDate.atTime(23, 59, 59));

        period.setDeleted(false);
        return period;
    }

    /**
     * 创建季度期间
     */
    private BudgetPeriod createQuarterlyPeriod(String year, String month, int yearInt, int startMonth, int endMonth) {
        BudgetPeriod period = new BudgetPeriod();
        period.setId(identifierGenerator.nextId(null).longValue());
        period.setYear(year);
        period.setMonth(month);
        period.setPeriodType(PERIOD_TYPE_QUARTERLY);

        // 计算开始日期和结束日期
        LocalDate startDate = LocalDate.of(yearInt, startMonth, 1);
        YearMonth endYearMonth = YearMonth.of(yearInt, endMonth);
        LocalDate endDate = endYearMonth.atEndOfMonth();

        period.setStartDate(startDate.atStartOfDay());
        period.setEndDate(endDate.atTime(23, 59, 59));

        period.setDeleted(false);
        return period;
    }

    /**
     * 创建半年度期间
     */
    private BudgetPeriod createHalfYearlyPeriod(String year, String month, int yearInt, int startMonth, int endMonth) {
        BudgetPeriod period = new BudgetPeriod();
        period.setId(identifierGenerator.nextId(null).longValue());
        period.setYear(year);
        period.setMonth(month);
        period.setPeriodType(PERIOD_TYPE_HALFYEARLY);

        // 计算开始日期和结束日期
        LocalDate startDate = LocalDate.of(yearInt, startMonth, 1);
        YearMonth endYearMonth = YearMonth.of(yearInt, endMonth);
        LocalDate endDate = endYearMonth.atEndOfMonth();

        period.setStartDate(startDate.atStartOfDay());
        period.setEndDate(endDate.atTime(23, 59, 59));

        period.setDeleted(false);
        return period;
    }

    /**
     * 创建年度期间
     */
    private BudgetPeriod createYearlyPeriod(String year, int yearInt) {
        BudgetPeriod period = new BudgetPeriod();
        period.setId(identifierGenerator.nextId(null).longValue());
        period.setYear(year);
        period.setMonth("01"); // 年度数据月份设为01
        period.setPeriodType(PERIOD_TYPE_YEARLY);

        // 计算开始日期和结束日期
        LocalDate startDate = LocalDate.of(yearInt, 1, 1);
        LocalDate endDate = LocalDate.of(yearInt, 12, 31);

        period.setStartDate(startDate.atStartOfDay());
        period.setEndDate(endDate.atTime(23, 59, 59));

        period.setDeleted(false);
        return period;
    }
}

