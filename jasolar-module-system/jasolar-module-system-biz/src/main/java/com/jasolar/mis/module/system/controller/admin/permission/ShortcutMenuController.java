package com.jasolar.mis.module.system.controller.admin.permission;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuRespVO;
import com.jasolar.mis.module.system.controller.admin.permission.vo.ShortcutMenuSaveReqVO;
import com.jasolar.mis.module.system.service.permission.ShortcutMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jasolar.mis.framework.common.pojo.CommonResult.success;



@Tag(name = "管理后台 - 用户快捷菜单")
@RestController
@RequestMapping("/system/shortcut-menu")
@Validated
public class ShortcutMenuController {

    @Resource
    private ShortcutMenuService shortcutMenuService;

    @PostMapping("/save")
    @Operation(summary = "创建用户快捷菜单")
    public CommonResult<Boolean> saveShortcutMenu(@RequestBody List<ShortcutMenuSaveReqVO> createReqVOs) {
        return success(shortcutMenuService.saveShortcutMenu(createReqVOs));
    }


    @GetMapping("/list")
    @Operation(summary = "获得用户快捷菜单列表")
    public CommonResult<List<ShortcutMenuRespVO>> listShortcutMenu() {
        List<ShortcutMenuRespVO> list = shortcutMenuService.listShortcutMenu();
        return success(list);
    }


}