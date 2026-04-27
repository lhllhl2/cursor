package com.jasolar.mis.module.system.api.permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jasolar.mis.bizapi.Apis;
import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.permission.dto.DataPermissionDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@FeignClient(name = Apis.SYSTEM)
@Tag(name = "RPC 服务 - 权限")
public interface PermissionApi {

    String PREFIX = Apis.SYSTEM_PREFIX + "/permission";

    /** 提供给 Gateway 使用,访问此服务接口的路径 */
    String SERVICE_URI = "http://" + Apis.SYSTEM + PREFIX;

    @GetMapping(PREFIX + "/user-role-id-list-by-role-id")
    @Operation(summary = "获得拥有多个角色的用户编号集合")
    @Parameter(name = "roleIds", description = "角色编号集合", example = "1,2", required = true)
    CommonResult<Set<Long>> getUserRoleIdListByRoleIds(@RequestParam("roleIds") Collection<Long> roleIds);

    @GetMapping(PREFIX + "/has-any-permissions")
    @Operation(summary = "判断是否有权限，任一一个即可")
    @Parameter(name = "userId", description = "用户编号", example = "1", required = true)
    @Parameter(name = "permissions", description = "权限", example = "read,write", required = true)
    CommonResult<Boolean> hasAnyPermissions(@RequestParam("userId") Long userId, @RequestParam("permissions") String... permissions);

    @GetMapping(PREFIX + "/user-permissions")
    @Operation(summary = "查询用户的所有权限标识")
    CommonResult<Set<String>> findUserPermissions(
            @Parameter(description = "用户工号", required = true) @RequestParam("userName") String userNo);

    @GetMapping(PREFIX + "/has-any-roles")
    @Operation(summary = "判断是否有角色，任一一个即可")
    @Parameter(name = "userId", description = "用户编号", example = "1", required = true)
    @Parameter(name = "roles", description = "角色数组", example = "2", required = true)
    CommonResult<Boolean> hasAnyRoles(@RequestParam("userId") Long userId, @RequestParam("roles") String... roles);

    // @GetMapping(PREFIX + "/get-dept-data-permission")
    // @Operation(summary = "获得登陆用户的部门数据权限")
    // @Parameter(name = "userId", description = "用户编号", example = "2", required = true)
    // CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(@RequestParam("userId") Long userId);
    //
    //
    // @GetMapping(PREFIX + "/new/get-dept-data-permission")
    // @Operation(summary = "获得登陆用户的部门数据权限")
    // @Parameter(name = "userId", description = "用户编号", example = "2", required = true)
    // CommonResult<Map<ScopeTypeEnum, DataPermissionDTO>> getDeptDataPermissionNew(@RequestParam("userId") Long userId);

    @GetMapping(PREFIX + "/user-data-permissions")
    @Operation(summary = "获得登陆用户的部门数据权限")
    @Parameter(name = "userNo", description = "用户编号", example = "1", required = true)
    CommonResult<List<DataPermissionDTO>> getUserDataPermissions(@RequestParam("userNo") String userNo);

    @GetMapping(PREFIX + "/get-roleId-list-by-userId")
    @Operation(summary = "获取用户拥有的角色ID列表")
    @Parameter(name = "userId", description = "用户Id", example = "1", required = true)
    CommonResult<Set<Long>> getUserRoleIdListByUserIdFromCache(@RequestParam("userId") Long userId);

    /**
     * 查询角色的接口权限, 此方法会缓存权限
     * 
     * @param roleIds
     * @return
     */
    @GetMapping(PREFIX + "/get-interface-list-by-roleIds")
    @Operation(summary = "获取角色关联的接口列表")
    @Parameter(name = "roleIds", description = "角色ID", example = "1", required = true)
    CommonResult<Map<Long, Set<String>>> getInterfacesByRoleIds(@RequestParam("roleIds") Set<Long> roleIds);

}