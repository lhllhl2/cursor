package com.jasolar.mis.module.system.controller.admin.i18n;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.redis.util.RedisUtils;
import com.jasolar.mis.module.system.controller.admin.i18n.vo.SystemI18nMenuReqVO;
import com.jasolar.mis.module.system.controller.admin.i18n.vo.SystemI18nMenuTreeRespVO;
import com.jasolar.mis.module.system.service.admin.i18n.SystemI18nMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 菜单国际化管理")
@RestController
@RequestMapping("/system/i18n-menu")
@Validated
@Slf4j
public class SystemI18nMenuController {

    @Resource
    private SystemI18nMenuService systemI18nMenuService;

    @PostMapping("/save")
    @Operation(summary = "保存或更新菜单国际化配置")
    public CommonResult<Boolean> saveOrUpdateI18nConfig(@Valid @RequestBody SystemI18nMenuReqVO reqVO) {
        boolean result = systemI18nMenuService.saveOrUpdateI18nConfig(
                reqVO.getMenuId(), 
                reqVO.getJsonData()
        );
        
        // 如果保存或更新成功，清除Redis缓存
        if (result) {
            try {
                // 清除key为i18n的所有数据
                RedisUtils.del("i18n");
                log.info("清除Redis缓存成功 - key: i18n");
            } catch (Exception e) {
                log.error("清除Redis缓存失败 - key: i18n", e);
                // 缓存清除失败不影响主流程，只记录日志
            }
        }
        
        return success(result);
    }
    


    @GetMapping("/get")
    @Operation(summary = "根据菜单ID查询国际化配置")
    public CommonResult<Map<String, Map<String, String>>> getI18nConfigByMenuId(
            @Parameter(description = "菜单ID", required = true) @RequestParam("menuId") Long menuId) {
        Map<String, Map<String, String>> result = systemI18nMenuService.getI18nConfigByMenuId(menuId);
        return success(result);
    }

    @GetMapping("/getLangs")
    @Operation(summary = "获取所有菜单的国际化配置（树形结构）")
    public CommonResult<List<SystemI18nMenuTreeRespVO>> getAllMenuI18nConfig() {
        // 首先尝试从Redis缓存获取（反序列化失败时 JsonUtils 会抛 RuntimeException，需降级走 DB，避免整页 500）
        List<SystemI18nMenuTreeRespVO> cachedResult = null;
        try {
            cachedResult = RedisUtils.get("i18n", List.class);
        } catch (Exception e) {
            log.warn("读取或解析 Redis 菜单国际化缓存失败，将删除 key=i18n 并从数据库重建: {}", e.getMessage());
            try {
                RedisUtils.del("i18n");
            } catch (Exception delEx) {
                log.warn("删除损坏的 i18n 缓存失败: {}", delEx.getMessage());
            }
        }

        if (cachedResult != null) {
            log.info("从Redis缓存获取菜单国际化配置成功");
            return success(cachedResult);
        }
        
        // 如果缓存中没有，从数据库查询并组装
        List<SystemI18nMenuTreeRespVO> result = systemI18nMenuService.getAllMenuI18nConfig();
        
        // 将结果缓存到Redis，设置1小时过期时间
        try {
            RedisUtils.set("i18n", result, 3600);
            log.info("将菜单国际化配置缓存到Redis成功，key: i18n");
        } catch (Exception e) {
            log.error("缓存菜单国际化配置到Redis失败", e);
            // 缓存失败不影响主流程，只记录日志
        }
        
        return success(result);
    }

} 