package com.jasolar.mis.module.system.controller.admin.role;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.framework.common.pojo.PageResult;
import com.jasolar.mis.framework.common.pojo.PrimaryParam;
import com.jasolar.mis.module.system.controller.admin.permission.vo.menu.MenuRespVO;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleEditVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleMenuTreeReqVO;
import com.jasolar.mis.module.system.controller.admin.role.vo.RolePageVo;
import com.jasolar.mis.module.system.controller.admin.role.vo.RoleSaveVo;
import com.jasolar.mis.module.system.controller.admin.usergroup.resp.GroupUserByRoleResp;
import com.jasolar.mis.module.system.domain.admin.role.SystemRoleDo;
import com.jasolar.mis.module.system.service.admin.role.SystemRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 24/07/2025 17:25
 * Version : 1.0
 */
@Tag(name = "管理后台 - 角色")
@RestController
@RequestMapping("/system/role")
@Slf4j
public class SystemRoleController {

    @Autowired
    private SystemRoleService systemRoleService;

    @Operation(summary = "1.新增角色")
    @PostMapping(value = "/saveRole")
    public CommonResult<Void> saveRole(@RequestBody @Validated RoleSaveVo roleSaveVo){
        systemRoleService.saveRole(roleSaveVo);
        return CommonResult.success();
    }

    @Operation(summary = "2.角色分页查询")
    @PostMapping(value = "/rolePage")
    public CommonResult<PageResult<SystemRoleDo>> rolePage(@RequestBody RolePageVo rolePageVo){
        PageResult<SystemRoleDo> pageResult = systemRoleService.rolePage(rolePageVo);
        return CommonResult.success(pageResult);
    }

    @PostMapping(value = "/del")
    public CommonResult<Void> delete(@RequestBody PrimaryParam primaryParam){
        systemRoleService.delete(primaryParam);

        return CommonResult.success();
    }

    @Operation(summary = "3.角色修改")
    @PostMapping(value = "/editRole")
    public CommonResult<Void> edit(@RequestBody RoleEditVo roleEditVo){
        systemRoleService.editRole(roleEditVo);
        return CommonResult.success();
    }

    @PostMapping(value = "/getMenuTreeByRoleId")
    public CommonResult<List<MenuRespVO>> getMenuTreeByRoleId(@RequestBody RoleMenuTreeReqVO reqVO) {
        List<MenuRespVO> menuTree = systemRoleService.getMenuTreeByRoleId(reqVO.getRoleId());
        return CommonResult.success(menuTree);
    }

    @Operation(summary = "4.根据角色id查询用户组")
    @PostMapping(value = "/searchUserGroupByRole")
    public CommonResult<List<GroupUserByRoleResp>> searchUserGroupByRole(@RequestBody PrimaryParam primaryParam){
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("=== 开始查询 searchUserGroupByRole ===");
        log.info("线程: {}, roleId: {}, 时间: {}", threadName, primaryParam.getId(), startTime);
        
        // 打印 Tomcat 线程池状态
        printTomcatThreadPoolStatus("searchUserGroupByRole");
        
        // 连接池状态监控已移除，避免JMX访问失败警告
        
        try {
            List<GroupUserByRoleResp> resp = systemRoleService.searchUserGroupByRole(primaryParam);
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== 查询完成 ===");
            log.info("线程: {}, 耗时: {}ms, 结果数量: {}", threadName, duration, resp.size());
            return CommonResult.success(resp);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("=== 查询失败 ===");
            log.error("线程: {}, 耗时: {}ms, 错误: {}", threadName, duration, e.getMessage(), e);
            throw e;
        }
    }



    /**
     * 打印 Tomcat 线程池状态
     */
    private void printTomcatThreadPoolStatus(String methodName) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            
            // 查找 Tomcat 线程池 MBean
            Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null);
            
            if (objectNames.isEmpty()) {
                log.info("=== Tomcat 线程池状态 [{}] ===", methodName);
                log.info("未找到 Tomcat 线程池 MBean");
                return;
            }
            
            // 使用第一个找到的 Tomcat 线程池
            ObjectName objectName = objectNames.iterator().next();
            
            Integer currentThreadCount = (Integer) mBeanServer.getAttribute(objectName, "currentThreadCount");
            Integer currentThreadsBusy = (Integer) mBeanServer.getAttribute(objectName, "currentThreadsBusy");
            Integer maxThreads = (Integer) mBeanServer.getAttribute(objectName, "maxThreads");
            Integer minSpareThreads = (Integer) mBeanServer.getAttribute(objectName, "minSpareThreads");
            
            log.info("=== Tomcat 线程池状态 [{}] ===", methodName);
            log.info("当前线程数: {}, 忙碌线程数: {}, 最大线程数: {}, 最小空闲线程数: {}", 
                    currentThreadCount, currentThreadsBusy, maxThreads, minSpareThreads);
            
            // 如果忙碌线程数接近最大线程数，说明线程池可能有问题
            if (maxThreads > 0 && currentThreadsBusy > maxThreads * 0.8) {
                log.warn("警告: Tomcat 线程池使用率过高！忙碌线程: {}/{}, 使用率: {}%", 
                        currentThreadsBusy, maxThreads, (currentThreadsBusy * 100 / maxThreads));
            }
            
        } catch (Exception e) {
            log.info("=== Tomcat 线程池状态 [{}] ===", methodName);
            log.info("Tomcat 线程池状态获取失败: {} (这是正常的，如果未启用 JMX 监控)", e.getMessage());
        }
    }

    /**
     * 打印连接池状态
     */
    private void printConnectionPoolStatus(String methodName) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("com.zaxxer.hikari:type=Pool (master)");
            
            Integer activeConnections = (Integer) mBeanServer.getAttribute(objectName, "ActiveConnections");
            Integer idleConnections = (Integer) mBeanServer.getAttribute(objectName, "IdleConnections");
            Integer totalConnections = (Integer) mBeanServer.getAttribute(objectName, "TotalConnections");
            Integer threadsAwaitingConnection = (Integer) mBeanServer.getAttribute(objectName, "ThreadsAwaitingConnection");
            
            log.info("=== 连接池状态 [{}] ===", methodName);
            log.info("活跃连接: {}, 空闲连接: {}, 总连接: {}, 等待连接线程: {}", 
                    activeConnections, idleConnections, totalConnections, threadsAwaitingConnection);
            
            // 如果等待连接线程大于0，说明连接池可能有问题
            if (threadsAwaitingConnection > 0) {
                log.warn("警告: 有 {} 个线程正在等待数据库连接！", threadsAwaitingConnection);
            }
            
        } catch (Exception e) {
            log.warn("无法获取连接池状态: {}", e.getMessage());
        }
    }

}
