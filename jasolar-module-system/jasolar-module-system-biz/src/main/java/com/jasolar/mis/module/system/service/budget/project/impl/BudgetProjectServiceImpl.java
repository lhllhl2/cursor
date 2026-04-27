package com.jasolar.mis.module.system.service.budget.project.impl;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jasolar.mis.module.system.domain.project.SystemProject;
import com.jasolar.mis.module.system.mapper.admin.project.SystemProjectMapper;
import com.jasolar.mis.module.system.service.budget.project.BudgetProjectService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 项目预算 Service 实现类
 *
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Service
@Slf4j
public class BudgetProjectServiceImpl implements BudgetProjectService {

    @Resource
    private SystemProjectMapper systemProjectMapper;

    @Resource
    private IdentifierGenerator identifierGenerator;

    // 项目类型前缀
    private static final String[] PROJECT_TYPE_PREFIXES = {
            "PRJ", "PROJ", "PJ", "PRO", "PR"
    };

    // 项目类型后缀
    private static final String[] PROJECT_TYPE_SUFFIXES = {
            "DEV", "TEST", "PROD", "DEMO", "POC", "R&D", "IMPL", "MIG", "UPG"
    };

    // 项目名称模板（更真实的项目名称）
    private static final String[] PROJECT_NAME_TEMPLATES = {
            "晶澳太阳能光伏发电项目", "新能源管理系统", "智能监控平台", "数字化运营系统",
            "光伏电站建设项目", "能源管理系统", "生产管理系统", "质量管理系统",
            "财务管理系统", "人力资源系统", "采购管理系统", "销售管理系统",
            "库存管理系统", "设备管理系统", "安全管理系统", "环保管理系统",
            "数据分析平台", "业务集成平台", "信息化改造项目", "系统升级项目",
            "晶澳一期光伏项目", "晶澳二期光伏项目", "晶澳三期光伏项目", "晶澳四期光伏项目",
            "分布式光伏项目", "集中式光伏项目", "储能系统项目", "智能运维平台",
            "晶澳研发中心项目", "晶澳测试平台", "晶澳生产优化项目", "晶澳销售网络项目",
            "晶澳采购优化项目", "晶澳财务优化项目", "晶澳人力优化项目", "晶澳质量提升项目",
            "晶澳安全提升项目", "晶澳环保提升项目", "晶澳数据分析项目", "晶澳业务集成项目",
            "晶澳信息化项目", "晶澳数字化项目", "晶澳智能化项目", "晶澳自动化项目",
            "晶澳2024年度项目", "晶澳2025年度项目", "晶澳2026年度项目", "晶澳2027年度项目",
            "晶澳东部区域项目", "晶澳西部区域项目", "晶澳南部区域项目", "晶澳北部区域项目",
            "晶澳华东区域项目", "晶澳华南区域项目", "晶澳华北区域项目", "晶澳华西区域项目",
            "晶澳示范项目", "晶澳试点项目", "晶澳推广项目", "晶澳标准化项目",
            "晶澳创新项目", "晶澳转型项目", "晶澳升级项目", "晶澳改造项目",
            "晶澳扩建项目", "晶澳新建项目", "晶澳技改项目", "晶澳维护项目",
            "晶澳运营优化项目", "晶澳成本控制项目", "晶澳效率提升项目", "晶澳产能提升项目",
            "晶澳技术研发项目", "晶澳产品开发项目", "晶澳市场拓展项目", "晶澳客户服务项目",
            "晶澳供应链优化项目", "晶澳物流优化项目", "晶澳仓储优化项目", "晶澳配送优化项目",
            "晶澳信息化建设", "晶澳数字化建设", "晶澳智能化建设", "晶澳自动化建设",
            "晶澳系统集成项目", "晶澳平台建设项目", "晶澳数据中心项目", "晶澳云计算项目",
            "晶澳大数据项目", "晶澳人工智能项目", "晶澳物联网项目", "晶澳区块链项目",
            "晶澳移动应用项目", "晶澳Web应用项目", "晶澳API接口项目", "晶澳微服务项目"
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateProjectData() {
        log.info("开始生成模拟项目数据");

        List<SystemProject> projectList = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= 100; i++) {
            SystemProject project = new SystemProject();
            // 使用雪花算法生成ID
            project.setId(identifierGenerator.nextId(null).longValue());
            
            // 生成项目编码：格式如 PRJ-DEV-001, PROJ-TEST-002, PJ-R&D-003 等
            // 为了增加多样性，前50个使用标准格式，后50个使用简化格式
            String projectCode;
            if (i <= 50) {
                String typePrefix = PROJECT_TYPE_PREFIXES[random.nextInt(PROJECT_TYPE_PREFIXES.length)];
                String typeSuffix = PROJECT_TYPE_SUFFIXES[random.nextInt(PROJECT_TYPE_SUFFIXES.length)];
                projectCode = String.format("%s-%s-%03d", typePrefix, typeSuffix, i);
            } else {
                // 后50个使用简化格式，如 PRJ001, PROJ002 等
                String typePrefix = PROJECT_TYPE_PREFIXES[random.nextInt(PROJECT_TYPE_PREFIXES.length)];
                projectCode = String.format("%s%03d", typePrefix, i);
            }
            project.setProjectCode(projectCode);
            
            // 生成项目名称：从模板中随机选择，如果超过模板数量则添加序号
            String projectName;
            if (i <= PROJECT_NAME_TEMPLATES.length) {
                projectName = PROJECT_NAME_TEMPLATES[i - 1];
            } else {
                // 超过模板数量时，从模板中随机选择并添加序号
                String baseName = PROJECT_NAME_TEMPLATES[random.nextInt(PROJECT_NAME_TEMPLATES.length)];
                projectName = baseName + "-" + String.format("%03d", i);
            }
            project.setProjectName(projectName);
            
            // PARENT_PROJECT_CODE 留空
            project.setParentProjectCode(null);
            
            // DELETED 设置为 false
            project.setDeleted(false);
            
            // CREATOR, CREATE_TIME, UPDATER, UPDATE_TIME 由 BaseDO 提供，框架会自动填充
            
            projectList.add(project);
        }

        // 批量插入
        boolean success = systemProjectMapper.insertBatch(projectList);
        int successCount = success ? projectList.size() : 0;

        String result = String.format("生成模拟项目数据完成！共生成 %d 条数据，成功插入 %d 条", projectList.size(), successCount);
        log.info(result);
        return result;
    }
}

