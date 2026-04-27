package com.jasolar.mis.module.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 预算科目编码配置
 * 用于配置需要特殊处理的科目编码前缀列表
 * 
 * @author Auto Generated
 * @date 2025-01-XX
 */
@Configuration
@ConfigurationProperties(prefix = "budget.subject-code")
@Data
public class BudgetSubjectCodeConfig {
    
    /**
     * 科目编码前缀白名单列表
     * 如果科目编码以这些前缀开头，将继续执行后续流程
     * 如果科目编码不以这些前缀开头，将直接返回合并后的参数，不执行后续流程
     * 配置格式：5101,6601,6602,M5101,M5201
     */
    private List<String> whitelistPrefixes = new ArrayList<>();
    
    /**
     * 检查科目编码是否在白名单中（是否以配置的前缀开头）
     * 
     * @param subjectCode 科目编码
     * @return true 如果科目编码以配置的前缀开头（在白名单中），false 否则
     */
    public boolean isInWhitelist(String subjectCode) {
        if (subjectCode == null || subjectCode.isEmpty() || whitelistPrefixes == null || whitelistPrefixes.isEmpty()) {
            // 如果配置为空，默认所有科目编码都不在白名单中（直接返回）
            return false;
        }
        
        // 过滤掉空字符串，检查是否以任何配置的前缀开头
        return whitelistPrefixes.stream()
                .filter(prefix -> prefix != null && !prefix.trim().isEmpty())
                .anyMatch(prefix -> subjectCode.startsWith(prefix.trim()));
    }
}

