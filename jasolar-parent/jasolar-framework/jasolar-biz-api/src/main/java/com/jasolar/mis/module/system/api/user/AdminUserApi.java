package com.jasolar.mis.module.system.api.user;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.util.collection.CollectionUtils;
import com.jasolar.mis.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@FeignClient(name = Apis.SYSTEM)
@Tag(name = "RPC 服务 - 管理员用户")
public interface AdminUserApi {

    String PREFIX = Apis.SYSTEM_PREFIX + "/user";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "通过用户 ID 查询用户")
    @Parameter(name = "id", description = "用户编号", example = "1", required = true)
    CommonResult<AdminUserRespDTO> getUser(@RequestParam("id") Long id);

    @GetMapping(PREFIX + "/list-by-subordinate")
    @Operation(summary = "通过用户 ID 查询用户下属")
    @Parameter(name = "id", description = "用户编号", example = "1", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserListBySubordinate(@RequestParam("id") Long id);

    @GetMapping(PREFIX + "/list")
    @Operation(summary = "通过用户 ID 查询用户们")
    @Parameter(name = "ids", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserList(@RequestParam("ids") Collection<Long> ids);

    @GetMapping(PREFIX + "/list-by-dept-id")
    @Operation(summary = "获得指定部门的用户数组")
    @Parameter(name = "deptIds", description = "部门编号数组", example = "1,2", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserListByDeptIds(@RequestParam("deptIds") Collection<Long> deptIds);

    @GetMapping(PREFIX + "/list-by-post-id")
    @Operation(summary = "获得指定岗位的用户数组")
    @Parameter(name = "postIds", description = "岗位编号数组", example = "2,3", required = true)
    CommonResult<List<AdminUserRespDTO>> getUserListByPostIds(@RequestParam("postIds") Collection<Long> postIds);

    @GetMapping(PREFIX + "/valid")
    @Operation(summary = "校验用户们是否有效")
    @Parameter(name = "ids", description = "用户编号数组", example = "3,5", required = true)
    CommonResult<Boolean> validateUserList(@RequestParam("ids") Collection<Long> ids);

    /**
     * 获得用户 Map
     *
     * @param ids 用户编号数组
     * @return 用户 Map
     */
    default Map<Long, AdminUserRespDTO> getUserMap(Collection<Long> ids) {
        List<AdminUserRespDTO> users = getUserList(ids).getCheckedData();
        return CollectionUtils.convertMap(users, AdminUserRespDTO::getId);
    }

    /**
     * 校验用户是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param id 用户编号
     */
    default void validateUser(Long id) {
        validateUserList(Collections.singleton(id));
    }

    /**
     * 通过工号查询
     * 
     * @param userNo 工号
     * @return 用户信息
     */
    @GetMapping(PREFIX + "/{userNo}")
    @Operation(summary = "通过 工号 查询用户")
    CommonResult<AdminUserRespDTO> getUser(@PathVariable("userNo") @Parameter(description = "用户编号") String userNo);

    /**
     * 通过工号批量查询
     *
     * @param userNos 工号
     * @return 用户信息
     */
    @PostMapping(PREFIX + "/list-by-nos")
    @Operation(summary = "通过 工号 批量查询用户")
    CommonResult<List<AdminUserRespDTO>> getUsers(@RequestBody List<String> userNos);

    /**
     * 根据角色编号，获得用户列表
     *
     * @param roleCode 角色编码
     * @return 用户列表
     */
    @Operation(summary = "根据角色编码，获得用户列表")
    @GetMapping(PREFIX + "/list-by-role-code")
    CommonResult<List<AdminUserRespDTO>> getUsersByRoleCode(@RequestParam("roleCode") String roleCode);

}
