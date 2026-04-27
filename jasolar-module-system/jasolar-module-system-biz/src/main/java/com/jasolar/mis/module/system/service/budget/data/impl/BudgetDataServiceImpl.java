package com.jasolar.mis.module.system.service.budget.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.budget.BudgetAccount;
import com.jasolar.mis.module.system.domain.budget.BudgetPeriod;
import com.jasolar.mis.module.system.domain.budget.SystemProjectBudget;
import com.jasolar.mis.module.system.domain.morg.SystemManagementOrganization;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.config.OracleSchemaInterceptor;
import com.jasolar.mis.module.system.mapper.budget.BudgetAccountMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetDataMapper;
import com.jasolar.mis.module.system.mapper.budget.BudgetPeriodMapper;
import com.jasolar.mis.module.system.mapper.budget.SystemProjectBudgetMapper;
import com.jasolar.mis.module.system.mapper.morg.SystemManagementOrganizationMapper;
import com.jasolar.mis.module.system.mapper.admin.project.SystemProjectMapper;
import com.jasolar.mis.module.system.service.budget.data.BudgetDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预算数据 Service 实现类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Service
@Slf4j
public class BudgetDataServiceImpl implements BudgetDataService {

    @Resource
    private BudgetDataMapper budgetDataMapper;

    @Resource
    private SystemProjectMapper systemProjectMapper;

    @Resource
    private BudgetAccountMapper budgetAccountMapper;

    @Resource
    private SystemManagementOrganizationMapper systemManagementOrganizationMapper;

    @Resource
    private BudgetPeriodMapper budgetPeriodMapper;

    @Resource
    private SystemProjectBudgetMapper systemProjectBudgetMapper;

    @Resource
    private OracleSchemaInterceptor oracleSchemaInterceptor;

    @Resource
    private IdentifierGenerator identifierGenerator;

    private static final String YEAR_2025 = "2025";
    private static final String CURRENCY_CNY = "CNY";
    private static final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String simulateBudgetData() {
        log.info("开始生成模拟预算数据");

        // 查询并打印当前 schema（在查询表之前）
        try {
            // 先打印配置的 schema
            String configuredSchema = oracleSchemaInterceptor != null ? 
                    oracleSchemaInterceptor.getSchemaForLogging() : "未知";
            log.info("========== 配置的 Schema: {} ==========", configuredSchema);
            
            // 再查询实际的 schema（这个查询会触发拦截器设置 schema）
            String currentSchema = systemProjectMapper.getCurrentSchema();
            log.info("========== 当前实际 Schema: {} ==========", currentSchema);
            log.info("========== 准备查询 SYSTEM_PROJECT 表 ==========");
        } catch (Exception e) {
            log.warn("查询当前 Schema 失败: {}", e.getMessage(), e);
        }

        // 1. 查询所有项目，然后随机选择 8 条
        List<SystemProject> allProjects = systemProjectMapper.selectList(
                new LambdaQueryWrapper<SystemProject>()
                        .eq(SystemProject::getDeleted, false)
        );
        if (allProjects.isEmpty()) {
            return "生成失败：SYSTEM_PROJECT 表中没有可用数据";
        }
        Collections.shuffle(allProjects, random);
        List<SystemProject> projects = allProjects.stream().limit(8).collect(Collectors.toList());
        log.info("随机选择了 {} 个项目", projects.size());

        // 2. 查询所有预算科目，然后随机选择 3 条
        List<BudgetAccount> allAccounts = budgetAccountMapper.selectList(
                new LambdaQueryWrapper<BudgetAccount>()
                        .eq(BudgetAccount::getDeleted, false)
        );
        if (allAccounts.isEmpty()) {
            return "生成失败：BUDGET_ACCOUNT 表中没有可用数据";
        }
        Collections.shuffle(allAccounts, random);
        List<BudgetAccount> accounts = allAccounts.stream().limit(3).collect(Collectors.toList());
        log.info("随机选择了 {} 个预算科目", accounts.size());

        // 3. 查询所有组织，过滤出 morg_code 唯一的，然后随机选择 5 个
        List<SystemManagementOrganization> allOrgs = systemManagementOrganizationMapper.selectList(
                new LambdaQueryWrapper<SystemManagementOrganization>()
                        .eq(SystemManagementOrganization::getDeleted, false)
        );
        if (allOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有可用数据";
        }
        // 统计每个 morg_code 出现的次数
        Map<String, Long> morgCodeCount = allOrgs.stream()
                .collect(Collectors.groupingBy(SystemManagementOrganization::getMorgCode, Collectors.counting()));
        // 过滤出 morg_code 唯一的组织
        List<SystemManagementOrganization> uniqueOrgs = allOrgs.stream()
                .filter(org -> morgCodeCount.get(org.getMorgCode()) == 1)
                .collect(Collectors.toList());
        if (uniqueOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有 morg_code 唯一的组织";
        }
        Collections.shuffle(uniqueOrgs, random);
        List<SystemManagementOrganization> organizations = uniqueOrgs.stream().limit(5).collect(Collectors.toList());
        log.info("选择了 {} 个 morg_code 唯一的组织", organizations.size());

        // 4. 验证 SYSTEM_PERIOD 表中有 2025 年的期间数据（用于获取年份信息）
        List<BudgetPeriod> periods = budgetPeriodMapper.selectList(
                new LambdaQueryWrapper<BudgetPeriod>()
                        .eq(BudgetPeriod::getYear, YEAR_2025)
                        .eq(BudgetPeriod::getDeleted, false)
                        .in(BudgetPeriod::getPeriodType, "MONTHLY", "QUARTERLY", "YEARLY")
        );
        if (periods.isEmpty()) {
            return "生成失败：SYSTEM_PERIOD 表中没有 2025 年的期间数据";
        }
        log.info("验证了 {} 个 2025 年的期间数据", periods.size());

        // 5. 生成预算数据
        // 为每个项目、科目、组织的组合生成一条完整的年度预算记录
        List<SystemProjectBudget> budgetList = new ArrayList<>();

        for (SystemProject project : projects) {
            for (BudgetAccount account : accounts) {
                for (SystemManagementOrganization org : organizations) {
                    // 仅生成集团外
                    SystemProjectBudget externalBudget = createAnnualBudget(project, account, org, "0");
                    budgetList.add(externalBudget);
                }
            }
        }

        // 批量插入
        boolean success = budgetDataMapper.insertBatch(budgetList);
        int successCount = success ? budgetList.size() : 0;

        String result = String.format("生成模拟预算数据完成！共生成 %d 条数据，成功插入 %d 条", budgetList.size(), successCount);
        log.info(result);
        return result;
    }

    /**
     * 创建年度预算数据（包含完整的月度、季度、年度数据）
     */
    private SystemProjectBudget createAnnualBudget(
            SystemProject project,
            BudgetAccount account,
            SystemManagementOrganization org,
            String isInternal
    ) {
        SystemProjectBudget budget = new SystemProjectBudget();
        budget.setId(identifierGenerator.nextId(null).longValue());
        budget.setAccount(account.getAccountSubjectCode());
        budget.setYear(YEAR_2025);
        budget.setVersion(null);
        budget.setScenario(null);
        budget.setCustom1(account.getCustomCode());
        budget.setCustom2(org.getMorgCode());
        budget.setCustom3(null);
        budget.setProject(project.getProjectCode());
        budget.setIsInternal(isInternal);
        budget.setCurrency(CURRENCY_CNY);

        // 生成月度预算数据，模拟真实情况
        // 基础年度总额范围：120万到1200万
        BigDecimal baseYearTotal = generateRandomAmount(1200000, 12000000);
        
        // 生成12个月的预算数据，模拟季节性变化
        // 先计算各月的权重（考虑季节性因素）
        double[] monthlyWeights = new double[12];
        double totalWeight = 0.0;
        for (int i = 0; i < 12; i++) {
            int month = i + 1;
            double seasonalFactor = getSeasonalFactor(month);
            double variation = 0.9 + random.nextDouble() * 0.2; // ±10% 随机波动
            monthlyWeights[i] = seasonalFactor * variation;
            totalWeight += monthlyWeights[i];
        }
        
        // 根据权重分配月度金额
        BigDecimal jan = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[0] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal feb = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[1] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mar = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[2] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal apr = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[3] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal may = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[4] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jun = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[5] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jul = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[6] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aug = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[7] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sep = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[8] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oct = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[9] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal nov = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[10] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dec = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[11] / totalWeight)).setScale(2, RoundingMode.HALF_UP);

        budget.setJan(jan);
        budget.setFeb(feb);
        budget.setMar(mar);
        budget.setApr(apr);
        budget.setMay(may);
        budget.setJun(jun);
        budget.setJul(jul);
        budget.setAug(aug);
        budget.setSep(sep);
        budget.setOct(oct);
        budget.setNov(nov);
        budget.setDec(dec);

        // 计算季度总额
        BigDecimal q1 = jan.add(feb).add(mar);
        BigDecimal q2 = apr.add(may).add(jun);
        BigDecimal q3 = jul.add(aug).add(sep);
        BigDecimal q4 = oct.add(nov).add(dec);

        budget.setQ1(q1);
        budget.setQ2(q2);
        budget.setQ3(q3);
        budget.setQ4(q4);

        // 计算年度总额
        BigDecimal yearTotal = q1.add(q2).add(q3).add(q4);
        budget.setYearTotal(yearTotal);

        budget.setDeleted(false);
        return budget;
    }

    /**
     * 生成随机金额
     */
    private BigDecimal generateRandomAmount(double min, double max) {
        double amount = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取季节性因子
     */
    private double getSeasonalFactor(int month) {
        // 模拟季节性：年初和年末较高，年中较低
        if (month <= 3 || month >= 11) {
            // 年初和年末：1.0 - 1.2倍
            return 1.0 + random.nextDouble() * 0.2;
        } else if (month >= 4 && month <= 6) {
            // 第二季度：0.8 - 1.0倍
            return 0.8 + random.nextDouble() * 0.2;
        } else {
            // 第三季度：0.9 - 1.1倍
            return 0.9 + random.nextDouble() * 0.2;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String simulateBudgetDataWithoutProject() {
        log.info("========== 开始模拟生成预算数据（不包含项目维度） ==========");

        // 步骤一：查询所有预算科目，排除已存在的科目，然后随机选择 3 条
        // 1.1 查询所有预算科目
        List<BudgetAccount> allAccounts = budgetAccountMapper.selectList(
                new LambdaQueryWrapper<BudgetAccount>()
                        .eq(BudgetAccount::getDeleted, false)
        );
        if (allAccounts.isEmpty()) {
            return "生成失败：BUDGET_ACCOUNT 表中没有可用数据";
        }
        log.info("查询到 {} 个预算科目", allAccounts.size());

        // 1.2 查询 SYSTEM_PROJECT_BUDGET，获取所有已存在的 CUSTOM1+"-"+ACCOUNT 组合
        List<SystemProjectBudget> existingBudgets = budgetDataMapper.selectList(
                new LambdaQueryWrapper<SystemProjectBudget>()
                        .eq(SystemProjectBudget::getDeleted, false)
                        .isNotNull(SystemProjectBudget::getCustom1)
                        .isNotNull(SystemProjectBudget::getAccount)
        );
        Set<String> existingAccountKeys = existingBudgets.stream()
                .map(budget -> budget.getCustom1() + "-" + budget.getAccount())
                .collect(Collectors.toSet());
        log.info("已存在 {} 个科目组合", existingAccountKeys.size());

        // 1.3 过滤掉已存在的科目
        List<BudgetAccount> availableAccounts = allAccounts.stream()
                .filter(account -> {
                    String key = account.getCustomCode() + "-" + account.getAccountSubjectCode();
                    return !existingAccountKeys.contains(key);
                })
                .collect(Collectors.toList());
        if (availableAccounts.isEmpty()) {
            return "生成失败：所有预算科目都已存在，没有可用的科目";
        }
        log.info("过滤后可用的预算科目有 {} 个", availableAccounts.size());

        // 1.4 随机选择 1 条
        Collections.shuffle(availableAccounts, random);
        List<BudgetAccount> selectedAccounts = availableAccounts.stream()
                .limit(1)
                .collect(Collectors.toList());
        log.info("随机选择了 {} 个预算科目", selectedAccounts.size());

        // 步骤二：查询所有组织，过滤出 morg_code 唯一的，排除已存在的组织，然后随机选择 1 条
        // 2.1 查询所有组织
        List<SystemManagementOrganization> allOrgs = systemManagementOrganizationMapper.selectList(
                new LambdaQueryWrapper<SystemManagementOrganization>()
                        .eq(SystemManagementOrganization::getDeleted, false)
        );
        if (allOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有可用数据";
        }
        log.info("查询到 {} 个组织", allOrgs.size());

        // 2.2 统计每个 morg_code 出现的次数
        Map<String, Long> morgCodeCount = allOrgs.stream()
                .collect(Collectors.groupingBy(SystemManagementOrganization::getMorgCode, Collectors.counting()));
        
        // 2.3 过滤出 morg_code 唯一的组织
        List<SystemManagementOrganization> uniqueOrgs = allOrgs.stream()
                .filter(org -> morgCodeCount.get(org.getMorgCode()) == 1)
                .collect(Collectors.toList());
        if (uniqueOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有 morg_code 唯一的组织";
        }
        log.info("过滤出 {} 个 morg_code 唯一的组织", uniqueOrgs.size());

        // 2.4 查询 SYSTEM_PROJECT_BUDGET，获取所有已存在的 CUSTOM2
        Set<String> existingOrgKeys = existingBudgets.stream()
                .filter(budget -> budget.getCustom2() != null)
                .map(SystemProjectBudget::getCustom2)
                .collect(Collectors.toSet());
        log.info("已存在 {} 个组织", existingOrgKeys.size());

        // 2.5 过滤掉已存在的组织
        List<SystemManagementOrganization> availableOrgs = uniqueOrgs.stream()
                .filter(org -> !existingOrgKeys.contains(org.getMorgCode()))
                .collect(Collectors.toList());
        if (availableOrgs.isEmpty()) {
            return "生成失败：所有 morg_code 唯一的组织都已存在，没有可用的组织";
        }
        log.info("过滤后可用的组织有 {} 个", availableOrgs.size());

        // 2.6 随机选择 1 条
        Collections.shuffle(availableOrgs, random);
        SystemManagementOrganization selectedOrg = availableOrgs.get(0);
        log.info("随机选择了组织: {}", selectedOrg.getMorgCode());

        // 步骤三：验证 SYSTEM_PERIOD 表中有 2025 年的期间数据（用于获取年份信息）
        List<BudgetPeriod> periods = budgetPeriodMapper.selectList(
                new LambdaQueryWrapper<BudgetPeriod>()
                        .eq(BudgetPeriod::getYear, YEAR_2025)
                        .eq(BudgetPeriod::getDeleted, false)
                        .in(BudgetPeriod::getPeriodType, "MONTHLY", "QUARTERLY", "YEARLY")
        );
        if (periods.isEmpty()) {
            return "生成失败：SYSTEM_PERIOD 表中没有 2025 年的期间数据";
        }
        log.info("验证了 {} 个 2025 年的期间数据", periods.size());

        // 步骤四：生成预算数据
        // 为每个科目、组织（项目为空）的组合生成一条完整的年度预算记录
        List<SystemProjectBudget> budgetList = new ArrayList<>();

        for (BudgetAccount account : selectedAccounts) {
            // 不含项目默认集团内，仅生成一条
            SystemProjectBudget internalBudget = createAnnualBudgetWithoutProject(account, selectedOrg, "1");
            budgetList.add(internalBudget);
        }

        // 步骤五：插入数据库
        boolean success = budgetDataMapper.insertBatch(budgetList);
        int successCount = success ? budgetList.size() : 0;

        String result = String.format("生成模拟预算数据（不包含项目维度）完成！共生成 %d 条数据，成功插入 %d 条", budgetList.size(), successCount);
        log.info("========== {} ==========", result);
        return result;
    }

    /**
     * 创建年度预算数据（不包含项目维度）
     */
    private SystemProjectBudget createAnnualBudgetWithoutProject(
            BudgetAccount account,
            SystemManagementOrganization org,
            String isInternal
    ) {
        SystemProjectBudget budget = new SystemProjectBudget();
        budget.setId(identifierGenerator.nextId(null).longValue());
        budget.setAccount(account.getAccountSubjectCode());
        budget.setYear(YEAR_2025);
        budget.setVersion(null);
        budget.setScenario(null);
        budget.setCustom1(account.getCustomCode());
        budget.setCustom2(org.getMorgCode());
        budget.setCustom3(null);
        budget.setProject(null); // 不包含项目维度
        budget.setIsInternal(isInternal);
        budget.setCurrency(CURRENCY_CNY);

        // 生成月度预算数据，模拟真实情况
        // 基础年度总额范围：120万到1200万
        BigDecimal baseYearTotal = generateRandomAmount(1200000, 12000000);
        
        // 生成12个月的预算数据，模拟季节性变化
        // 先计算各月的权重（考虑季节性因素）
        double[] monthlyWeights = new double[12];
        double totalWeight = 0.0;
        for (int i = 0; i < 12; i++) {
            int month = i + 1;
            double seasonalFactor = getSeasonalFactor(month);
            double variation = 0.9 + random.nextDouble() * 0.2; // ±10% 随机波动
            monthlyWeights[i] = seasonalFactor * variation;
            totalWeight += monthlyWeights[i];
        }
        
        // 根据权重分配月度金额
        BigDecimal jan = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[0] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal feb = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[1] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mar = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[2] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal apr = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[3] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal may = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[4] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jun = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[5] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jul = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[6] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aug = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[7] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sep = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[8] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oct = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[9] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal nov = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[10] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dec = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[11] / totalWeight)).setScale(2, RoundingMode.HALF_UP);

        budget.setJan(jan);
        budget.setFeb(feb);
        budget.setMar(mar);
        budget.setApr(apr);
        budget.setMay(may);
        budget.setJun(jun);
        budget.setJul(jul);
        budget.setAug(aug);
        budget.setSep(sep);
        budget.setOct(oct);
        budget.setNov(nov);
        budget.setDec(dec);

        // 计算季度总额
        BigDecimal q1 = jan.add(feb).add(mar);
        BigDecimal q2 = apr.add(may).add(jun);
        BigDecimal q3 = jul.add(aug).add(sep);
        BigDecimal q4 = oct.add(nov).add(dec);

        budget.setQ1(q1);
        budget.setQ2(q2);
        budget.setQ3(q3);
        budget.setQ4(q4);

        // 计算年度总额
        BigDecimal yearTotal = q1.add(q2).add(q3).add(q4);
        budget.setYearTotal(yearTotal);

        budget.setDeleted(false);
        return budget;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String simulateCapitalTypeBudgetData() {
        log.info("========== 开始模拟生成资金类型预算数据 ==========");

        // 步骤一：查询所有项目，排除已存在的项目，然后随机选择 1 条
        // 1.1 查询所有项目
        List<SystemProject> allProjects = systemProjectMapper.selectList(
                new LambdaQueryWrapper<SystemProject>()
                        .eq(SystemProject::getDeleted, false)
        );
        if (allProjects.isEmpty()) {
            return "生成失败：SYSTEM_PROJECT 表中没有可用数据";
        }
        log.info("查询到 {} 个项目", allProjects.size());

        // 1.2 查询 SYSTEM_PROJECT_BUDGET，获取所有已存在的 PROJECT
        List<SystemProjectBudget> existingBudgets = budgetDataMapper.selectList(
                new LambdaQueryWrapper<SystemProjectBudget>()
                        .eq(SystemProjectBudget::getDeleted, false)
                        .isNotNull(SystemProjectBudget::getProject)
        );
        Set<String> existingProjectKeys = existingBudgets.stream()
                .map(SystemProjectBudget::getProject)
                .collect(Collectors.toSet());
        log.info("已存在 {} 个项目", existingProjectKeys.size());

        // 1.3 过滤掉已存在的项目
        List<SystemProject> availableProjects = allProjects.stream()
                .filter(project -> !existingProjectKeys.contains(project.getProjectCode()))
                .collect(Collectors.toList());
        if (availableProjects.isEmpty()) {
            return "生成失败：所有项目都已存在，没有可用的项目";
        }
        log.info("过滤后可用的项目有 {} 个", availableProjects.size());

        // 1.4 随机选择 1 条
        Collections.shuffle(availableProjects, random);
        SystemProject selectedProject = availableProjects.get(0);
        log.info("随机选择了项目: {}", selectedProject.getProjectCode());

        // 步骤二：查询所有预算科目，排除已存在的科目，然后随机选择 2 条
        // 2.1 查询所有预算科目
        List<BudgetAccount> allAccounts = budgetAccountMapper.selectList(
                new LambdaQueryWrapper<BudgetAccount>()
                        .eq(BudgetAccount::getDeleted, false)
        );
        if (allAccounts.isEmpty()) {
            return "生成失败：BUDGET_ACCOUNT 表中没有可用数据";
        }
        log.info("查询到 {} 个预算科目", allAccounts.size());

        // 2.2 获取所有已存在的 CUSTOM1+"-"+ACCOUNT 组合
        Set<String> existingAccountKeys = existingBudgets.stream()
                .filter(budget -> budget.getCustom1() != null && budget.getAccount() != null)
                .map(budget -> budget.getCustom1() + "-" + budget.getAccount())
                .collect(Collectors.toSet());
        log.info("已存在 {} 个科目组合", existingAccountKeys.size());

        // 2.3 过滤掉已存在的科目
        List<BudgetAccount> availableAccounts = allAccounts.stream()
                .filter(account -> {
                    String key = account.getCustomCode() + "-" + account.getAccountSubjectCode();
                    return !existingAccountKeys.contains(key);
                })
                .collect(Collectors.toList());
        if (availableAccounts.size() < 2) {
            return "生成失败：可用的预算科目不足 2 条";
        }
        log.info("过滤后可用的预算科目有 {} 个", availableAccounts.size());

        // 2.4 随机选择 2 条（第1条作为采购额科目，第2条作为付款额科目）
        Collections.shuffle(availableAccounts, random);
        BudgetAccount purchaseAccount = availableAccounts.get(0); // 采购额科目
        BudgetAccount paymentAccount = availableAccounts.get(1);  // 付款额科目
        log.info("随机选择了采购额科目: {} 和付款额科目: {}", 
                purchaseAccount.getAccountSubjectCode(), paymentAccount.getAccountSubjectCode());

        // 步骤三：查询所有组织，过滤出 morg_code 唯一的，排除已存在的组织，然后随机选择 2 条
        // 3.1 查询所有组织
        List<SystemManagementOrganization> allOrgs = systemManagementOrganizationMapper.selectList(
                new LambdaQueryWrapper<SystemManagementOrganization>()
                        .eq(SystemManagementOrganization::getDeleted, false)
        );
        if (allOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有可用数据";
        }
        log.info("查询到 {} 个组织", allOrgs.size());

        // 3.2 统计每个 morg_code 出现的次数
        Map<String, Long> morgCodeCount = allOrgs.stream()
                .collect(Collectors.groupingBy(SystemManagementOrganization::getMorgCode, Collectors.counting()));
        
        // 3.3 过滤出 morg_code 唯一的组织
        List<SystemManagementOrganization> uniqueOrgs = allOrgs.stream()
                .filter(org -> morgCodeCount.get(org.getMorgCode()) == 1)
                .collect(Collectors.toList());
        if (uniqueOrgs.isEmpty()) {
            return "生成失败：SYSTEM_MANAGEMENT_ORGANIZATION 表中没有 morg_code 唯一的组织";
        }
        log.info("过滤出 {} 个 morg_code 唯一的组织", uniqueOrgs.size());

        // 3.4 获取所有已存在的 CUSTOM2
        Set<String> existingOrgKeys = existingBudgets.stream()
                .filter(budget -> budget.getCustom2() != null)
                .map(SystemProjectBudget::getCustom2)
                .collect(Collectors.toSet());
        log.info("已存在 {} 个组织", existingOrgKeys.size());

        // 3.5 过滤掉已存在的组织
        List<SystemManagementOrganization> availableOrgs = uniqueOrgs.stream()
                .filter(org -> !existingOrgKeys.contains(org.getMorgCode()))
                .collect(Collectors.toList());
        if (availableOrgs.size() < 2) {
            return "生成失败：可用的 morg_code 唯一的组织不足 2 条";
        }
        log.info("过滤后可用的组织有 {} 个", availableOrgs.size());

        // 3.6 随机选择 2 条
        Collections.shuffle(availableOrgs, random);
        SystemManagementOrganization org1 = availableOrgs.get(0);
        SystemManagementOrganization org2 = availableOrgs.get(1);
        log.info("随机选择了组织1: {} 和组织2: {}", org1.getMorgCode(), org2.getMorgCode());

        // 步骤四：随机模拟 2 个资产类型编码
        String assetType1 = generateAssetTypeCode();
        String assetType2 = generateAssetTypeCode();
        log.info("生成资产类型编码1: {} 和资产类型编码2: {}", assetType1, assetType2);

        // 步骤五：验证 SYSTEM_PERIOD 表中有 2025 年的期间数据（用于获取年份信息）
        List<BudgetPeriod> periods = budgetPeriodMapper.selectList(
                new LambdaQueryWrapper<BudgetPeriod>()
                        .eq(BudgetPeriod::getYear, YEAR_2025)
                        .eq(BudgetPeriod::getDeleted, false)
                        .in(BudgetPeriod::getPeriodType, "MONTHLY", "QUARTERLY", "YEARLY")
        );
        if (periods.isEmpty()) {
            return "生成失败：SYSTEM_PERIOD 表中没有 2025 年的期间数据";
        }
        log.info("验证了 {} 个 2025 年的期间数据", periods.size());

        // 步骤六：生成 4 条预算数据
        List<SystemProjectBudget> budgetList = new ArrayList<>();

        // 预算1/2：科目1(采购额)，组织1，项目1，资产类型1，集团内/外
        SystemProjectBudget budget1External = createCapitalTypeBudget(
                purchaseAccount, org1, selectedProject, assetType1, "0");
        budgetList.add(budget1External);
        log.info("生成预算1(集团外): 采购额科目={}, 组织={}, 项目={}, 资产类型={}", 
                purchaseAccount.getAccountSubjectCode(), org1.getMorgCode(), 
                selectedProject.getProjectCode(), assetType1);

        // 预算2：科目2(付款额)，组织1，项目1，资产类型1，仅集团外
        SystemProjectBudget budget2External = createCapitalTypeBudget(
                paymentAccount, org1, selectedProject, assetType1, "0");
        budgetList.add(budget2External);
        log.info("生成预算2(集团外): 付款额科目={}, 组织={}, 项目={}, 资产类型={}", 
                paymentAccount.getAccountSubjectCode(), org1.getMorgCode(), 
                selectedProject.getProjectCode(), assetType1);

        // 预算5/6：科目1(采购额)，组织2，资产类型2，无项目，集团内/外
        SystemProjectBudget budget3Internal = createCapitalTypeBudget(
                purchaseAccount, org2, null, assetType2, "1");
        budgetList.add(budget3Internal);
        log.info("生成预算3(集团内): 采购额科目={}, 组织={}, 项目=无, 资产类型={}", 
                purchaseAccount.getAccountSubjectCode(), org2.getMorgCode(), assetType2);

        // 预算4：科目2(付款额)，组织2，资产类型2，无项目，仅集团内
        SystemProjectBudget budget4Internal = createCapitalTypeBudget(
                paymentAccount, org2, null, assetType2, "1");
        budgetList.add(budget4Internal);
        log.info("生成预算4(集团内): 付款额科目={}, 组织={}, 项目=无, 资产类型={}", 
                paymentAccount.getAccountSubjectCode(), org2.getMorgCode(), assetType2);

        // 步骤七：批量插入数据库
        boolean success = budgetDataMapper.insertBatch(budgetList);
        int successCount = success ? budgetList.size() : 0;

        String result = String.format("生成资金类型预算数据完成！共生成 %d 条数据，成功插入 %d 条", budgetList.size(), successCount);
        log.info("========== {} ==========", result);
        return result;
    }

    /**
     * 生成资产类型编码（模拟真实场景）
     */
    private String generateAssetTypeCode() {
        // 资产类型前缀列表
        String[] assetPrefixes = {"FIX", "INT", "CUR", "LTI", "EQU", "INV", "REC", "PPE"};
        // 资产类别
        String[] assetCategories = {"ASSET", "EQUIP", "BUILD", "VEHICLE", "TECH", "FURNITURE"};
        
        String prefix = assetPrefixes[random.nextInt(assetPrefixes.length)];
        String category = assetCategories[random.nextInt(assetCategories.length)];
        int number = 1000 + random.nextInt(9000); // 1000-9999
        
        return prefix + "-" + category + "-" + number;
    }

    /**
     * 创建资金类型预算数据
     */
    private SystemProjectBudget createCapitalTypeBudget(
            BudgetAccount account,
            SystemManagementOrganization org,
            SystemProject project,
            String assetTypeCode,
            String isInternal
    ) {
        SystemProjectBudget budget = new SystemProjectBudget();
        budget.setId(identifierGenerator.nextId(null).longValue());
        budget.setAccount(account.getAccountSubjectCode());
        budget.setYear(YEAR_2025);
        budget.setVersion(null);
        budget.setScenario(null);
        budget.setCustom1(account.getCustomCode());
        budget.setCustom2(org.getMorgCode());
        budget.setCustom3(assetTypeCode); // 资产类型编码
        budget.setProject(project != null ? project.getProjectCode() : null);
        budget.setIsInternal(isInternal);
        budget.setCurrency(CURRENCY_CNY);

        // 生成月度预算数据，模拟真实情况
        // 基础年度总额范围：120万到1200万
        BigDecimal baseYearTotal = generateRandomAmount(1200000, 12000000);
        
        // 生成12个月的预算数据，模拟季节性变化
        // 先计算各月的权重（考虑季节性因素）
        double[] monthlyWeights = new double[12];
        double totalWeight = 0.0;
        for (int i = 0; i < 12; i++) {
            int month = i + 1;
            double seasonalFactor = getSeasonalFactor(month);
            double variation = 0.9 + random.nextDouble() * 0.2; // ±10% 随机波动
            monthlyWeights[i] = seasonalFactor * variation;
            totalWeight += monthlyWeights[i];
        }
        
        // 根据权重分配月度金额
        BigDecimal jan = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[0] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal feb = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[1] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mar = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[2] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal apr = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[3] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal may = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[4] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jun = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[5] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal jul = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[6] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal aug = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[7] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sep = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[8] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oct = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[9] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal nov = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[10] / totalWeight)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dec = baseYearTotal.multiply(BigDecimal.valueOf(monthlyWeights[11] / totalWeight)).setScale(2, RoundingMode.HALF_UP);

        budget.setJan(jan);
        budget.setFeb(feb);
        budget.setMar(mar);
        budget.setApr(apr);
        budget.setMay(may);
        budget.setJun(jun);
        budget.setJul(jul);
        budget.setAug(aug);
        budget.setSep(sep);
        budget.setOct(oct);
        budget.setNov(nov);
        budget.setDec(dec);

        // 计算季度总额
        BigDecimal q1 = jan.add(feb).add(mar);
        BigDecimal q2 = apr.add(may).add(jun);
        BigDecimal q3 = jul.add(aug).add(sep);
        BigDecimal q4 = oct.add(nov).add(dec);

        budget.setQ1(q1);
        budget.setQ2(q2);
        budget.setQ3(q3);
        budget.setQ4(q4);

        // 计算年度总额
        BigDecimal yearTotal = q1.add(q2).add(q3).add(q4);
        budget.setYearTotal(yearTotal);

        budget.setDeleted(false);
        return budget;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncProjectBudgetNew(String year) {
        log.info("开始同步项目预算数据（新方法），年份: {}", year);
        
        try {
            // 1. 删除指定年份的数据
            int deletedCount = systemProjectBudgetMapper.deleteByYear(year);
            log.info("年份 {} 删除了 {} 条旧数据", year, deletedCount);
            
            // 2. 查询5个场景的数据并插入
            List<SystemProjectBudget> allData = new ArrayList<>();
            
            // 场景1：(ACCOUNT_CD LIKE 'A01030301%' OR ACCOUNT_CD LIKE 'A01040102%') AND PROJECT_CD = 'P00' AND CUSTOM2_CD = 'CU200'
            List<SystemProjectBudget> scenario1 = systemProjectBudgetMapper.selectNewScenario1(year);
            if (scenario1 != null && !scenario1.isEmpty()) {
                processNewScenario1(scenario1, year);
                allData.addAll(scenario1);
                log.info("场景1查询到 {} 条数据", scenario1.size());
            }
            
            // 场景2：ACCOUNT_CD = 'A01030112' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
            List<SystemProjectBudget> scenario2 = systemProjectBudgetMapper.selectNewScenario2(year);
            if (scenario2 != null && !scenario2.isEmpty()) {
                processNewScenario2(scenario2, year);
                allData.addAll(scenario2);
                log.info("场景2查询到 {} 条数据", scenario2.size());
            }
            
            // 场景3：ACCOUNT_CD = 'A010301150102' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
            List<SystemProjectBudget> scenario3 = systemProjectBudgetMapper.selectNewScenario3(year);
            if (scenario3 != null && !scenario3.isEmpty()) {
                processNewScenario3(scenario3, year);
                allData.addAll(scenario3);
                log.info("场景3查询到 {} 条数据", scenario3.size());
            }
            
            // 场景4：ACCOUNT_CD = 'A010301150102' AND PROJECT_CD != 'P00' AND CUSTOM2_CD = 'CU200'
            List<SystemProjectBudget> scenario4 = systemProjectBudgetMapper.selectNewScenario4(year);
            if (scenario4 != null && !scenario4.isEmpty()) {
                processNewScenario4(scenario4, year);
                allData.addAll(scenario4);
                log.info("场景4查询到 {} 条数据", scenario4.size());
            }
            
            // 场景5：ACCOUNT_CD = 'A01030115010102' (从 VIEW_BUDGET_TO_CONTROL_TZE 查询)
            // 注意：场景5的金额全部放到 Q1 和 yearTotal 中
            List<SystemProjectBudget> scenario5 = systemProjectBudgetMapper.selectNewScenario5(year);
            if (scenario5 != null && !scenario5.isEmpty()) {
                processNewScenario5(scenario5, year);
                allData.addAll(scenario5);
                log.info("场景5查询到 {} 条数据", scenario5.size());
            }
            
            // 3. 批量插入数据
            if (!allData.isEmpty()) {
                // 为每条数据生成ID并设置默认值，同时进行精度控制
                for (SystemProjectBudget budget : allData) {
                    if (budget.getId() == null) {
                        budget.setId(identifierGenerator.nextId(null).longValue());
                    }
                    budget.setYear(year);
                    budget.setDeleted(false);
                    // 对所有数值字段进行精度控制 NUMBER(18,6)
                    budget.setQ1(roundToPrecision(budget.getQ1()));
                    budget.setQ2(roundToPrecision(budget.getQ2()));
                    budget.setQ3(roundToPrecision(budget.getQ3()));
                    budget.setQ4(roundToPrecision(budget.getQ4()));
                    budget.setYearTotal(roundToPrecision(budget.getYearTotal()));
                    // creator、updater、createTime、updateTime 会在 BaseDO 中自动填充
                }
                
                // 使用 MyBatis Plus 的 insertBatch 方法，会自动填充 creator、updater 等字段
                // 分批插入，每批1000条
                systemProjectBudgetMapper.insertBatch(allData, 1000);
                
                log.info("年份 {} 插入了 {} 条数据", year, allData.size());
                String result = String.format("同步完成：年份 %s 共插入 %d 条数据", year, allData.size());
                log.info(result);
                return result;
            } else {
                log.info("年份 {} 没有需要插入的数据", year);
                return String.format("同步完成：年份 %s 没有需要插入的数据", year);
            }
            
        } catch (Exception e) {
            log.error("同步项目预算数据失败（新方法），年份: {}", year, e);
            throw new RuntimeException("同步项目预算数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理新方法场景1的数据
     * ACCOUNT_CD LIKE 'A01030301%' AND PROJECT_CD = 'P00' AND CUSTOM2_CD = 'CU200'
     * CUSTOM1_CD --> CUSTOM1
     * CUSTOM2_CD --> CUSTOM3 (因为都是CU200，不设置)
     * PROJECT_CD --> PROJECT (因为都是P00，不设置)
     * ENTITY_CD --> CUSTOM2
     * ACCOUNT_CD --> ACCOUNT
     */
    private void processNewScenario1(List<SystemProjectBudget> data, String year) {
        for (SystemProjectBudget budget : data) {
            // CUSTOM1_CD -> CUSTOM1 (已在SQL中映射)
            // CUSTOM2_CD -> CUSTOM3 (因为都是CU200，不设置，保持为null)
            budget.setCustom3(null);
            // PROJECT_CD -> PROJECT (因为都是P00，不设置，保持为null)
            budget.setProject(null);
            // ENTITY_CD -> CUSTOM2 (已在SQL中映射)
            // ACCOUNT_CD -> ACCOUNT (已在SQL中映射)
            // 默认设置为集团内
            budget.setIsInternal("1");
            // 其他字段已在SQL中映射
        }
    }

    /**
     * 处理新方法场景2的数据
     * ACCOUNT_CD = 'A01030112' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
     * CUSTOM1_CD --> CUSTOM1 (因为ACCOUNT都是A01030112，不设置)
     * CUSTOM2_CD --> CUSTOM3
     * PROJECT_CD --> PROJECT (因为都是P00，不设置)
     * ENTITY_CD --> CUSTOM2
     * ACCOUNT_CD --> ACCOUNT (因为都是A01030112，不设置)
     */
    private void processNewScenario2(List<SystemProjectBudget> data, String year) {
        for (SystemProjectBudget budget : data) {
            // CUSTOM1_CD -> CUSTOM1 (因为ACCOUNT都是A01030112，不设置，保持为null)
            budget.setCustom1(null);
            // CUSTOM2_CD -> CUSTOM3 (已在SQL中映射)
            // PROJECT_CD -> PROJECT (因为都是P00，不设置，保持为null)
            budget.setProject(null);
            // ENTITY_CD -> CUSTOM2 (已在SQL中映射)
            // ACCOUNT_CD -> ACCOUNT (保留原始值A01030112，用于syncQuotaDataFromOriginal识别)
            // budget.setAccount(null); // 注释掉，保留account值以便后续同步使用
            // 默认设置为集团内
            budget.setIsInternal("1");
            // 其他字段已在SQL中映射
        }
    }

    /**
     * 处理新方法场景3的数据
     * ACCOUNT_CD = 'A010301150102' AND PROJECT_CD = 'P00' AND CUSTOM2_CD LIKE 'CU205%'
     * CUSTOM1_CD --> CUSTOM1 (因为ACCOUNT都是A010301150102，不设置)
     * CUSTOM2_CD --> CUSTOM3
     * PROJECT_CD --> PROJECT (因为都是P00，不设置)
     * ENTITY_CD --> CUSTOM2
     * ACCOUNT_CD --> ACCOUNT (因为都是A010301150102，不设置)
     */
    private void processNewScenario3(List<SystemProjectBudget> data, String year) {
        for (SystemProjectBudget budget : data) {
            // CUSTOM1_CD -> CUSTOM1 (因为ACCOUNT都是A010301150102，不设置，保持为null)
            budget.setCustom1(null);
            // CUSTOM2_CD -> CUSTOM3 (已在SQL中映射)
            // PROJECT_CD -> PROJECT (因为都是P00，不设置，保持为null)
            budget.setProject(null);
            // ENTITY_CD -> CUSTOM2 (已在SQL中映射)
            // ACCOUNT_CD -> ACCOUNT (保留原始值A010301150102，用于syncQuotaDataFromOriginal识别)
            // budget.setAccount(null); // 注释掉，保留account值以便后续同步使用
            // 默认设置为集团内
            budget.setIsInternal("1");
            // 其他字段已在SQL中映射
        }
    }

    /**
     * 处理新方法场景4的数据
     * ACCOUNT_CD = 'A010301150102' AND PROJECT_CD != 'P00' AND CUSTOM2_CD = 'CU200'
     * CUSTOM1_CD --> CUSTOM1 (因为ACCOUNT都是A010301150102，不设置)
     * CUSTOM2_CD --> CUSTOM3 (因为都是CU200，不设置)
     * PROJECT_CD --> PROJECT
     * ENTITY_CD --> CUSTOM2
     * ACCOUNT_CD --> ACCOUNT (因为都是A010301150102，不设置)
     */
    private void processNewScenario4(List<SystemProjectBudget> data, String year) {
        for (SystemProjectBudget budget : data) {
            // CUSTOM1_CD -> CUSTOM1 (因为ACCOUNT都是A010301150102，不设置，保持为null)
            budget.setCustom1(null);
            // CUSTOM2_CD -> CUSTOM3 (因为都是CU200，不设置，保持为null)
            budget.setCustom3(null);
            // PROJECT_CD -> PROJECT (已在SQL中映射)
            // ENTITY_CD -> CUSTOM2 (已在SQL中映射)
            // ACCOUNT_CD -> ACCOUNT (保留原始值A010301150102，用于syncQuotaDataFromOriginal识别)
            // budget.setAccount(null); // 注释掉，保留account值以便后续同步使用
            // 默认设置为集团外
            budget.setIsInternal("0");
            // 其他字段已在SQL中映射
        }
    }

    /**
     * 处理新方法场景5的数据
     * ACCOUNT_CD = 'A01030115010102'
     * BEGBALANCE（映射到 yearTotal）全部放到 Q1 和 yearTotal 中，Q2-Q4 置 0
     *
     * @param data 场景5的数据列表
     * @param year 年份（当前逻辑未使用，保留参数以兼容调用方）
     */
    private void processNewScenario5(List<SystemProjectBudget> data, String year) {
        for (SystemProjectBudget budget : data) {
            // 不需要项目、资产类型等维度，保持与之前注释一致的字段清理逻辑
            budget.setCustom1(null);
            budget.setCustom3(null);
            // ACCOUNT_CD -> ACCOUNT (保留原始值A01030115010102，用于syncQuotaDataFromOriginal识别)
            // budget.setAccount(null); // 注释掉，保留account值以便后续同步使用
            // 默认设置为集团外
            budget.setIsInternal("0");

            // BEGBALANCE 已通过 SQL 映射到 yearTotal，这里统一做精度控制
            BigDecimal begBalance = budget.getYearTotal() != null ? budget.getYearTotal() : BigDecimal.ZERO;
            begBalance = roundToPrecision(begBalance);

            // 全部金额放到 Q1，其他季度为 0
            budget.setQ1(begBalance);
            budget.setQ2(BigDecimal.ZERO);
            budget.setQ3(BigDecimal.ZERO);
            budget.setQ4(BigDecimal.ZERO);

            // yearTotal 保持与 BEGBALANCE 一致
            budget.setYearTotal(begBalance);
        }
    }

    /**
     * 将BigDecimal四舍五入到数据库精度 NUMBER(18,6)
     * 总共18位，其中6位小数位，整数部分最多12位
     * 
     * @param value 原始值
     * @return 精度控制后的值
     */
    private BigDecimal roundToPrecision(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        // 四舍五入到6位小数
        BigDecimal rounded = value.setScale(6, RoundingMode.HALF_UP);
        // 检查整数部分是否超过12位（999999999999.999999）
        BigDecimal maxValue = new BigDecimal("999999999999.999999");
        BigDecimal minValue = new BigDecimal("-999999999999.999999");
        if (rounded.compareTo(maxValue) > 0) {
            log.warn("数值 {} 超过最大精度，将被截断为 {}", value, maxValue);
            return maxValue;
        }
        if (rounded.compareTo(minValue) < 0) {
            log.warn("数值 {} 超过最小精度，将被截断为 {}", value, minValue);
            return minValue;
        }
        return rounded;
    }
}

