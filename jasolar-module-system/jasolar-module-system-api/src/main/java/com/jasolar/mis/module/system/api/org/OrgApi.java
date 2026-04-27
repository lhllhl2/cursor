package com.jasolar.mis.module.system.api.org;

import com.jasolar.mis.framework.common.pojo.CommonResult;
import com.jasolar.mis.module.system.api.org.vo.OrgListReqVO;
import com.jasolar.mis.module.system.api.org.vo.OrgRespVO;
import jakarta.validation.Valid;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 组织管理 Feign 接口
 * 
 * 对应关系：
 * name = "jasolar-system-service"：服务名称，相当于要调用的服务在注册中心的名称
 * contextId = "orgApi"：客户端上下文ID，用于区分不同的Feign客户端
 * path = "/admin-api/system/org"：直接调用服务，不经过 Gateway
 * 
 * 注意：由于框架自动为 controller.admin.** 包下的 @RestController 添加 /admin-api 前缀
 * 所以实际 Controller 路径是 /admin-api/system/org，而不是 /system/org
 * Gateway 路由配置中的 /admin-api/** 仅用于外部访问，内部服务间调用不需要经过 Gateway
 * 
 * @author jasolar
 */
@FeignClient(
    name = "jasolar-system-service", 
    contextId = "orgApi", 
    path = "/admin-api/system/org"
)
public interface OrgApi {

    /**
     * 查询组织树列表（不分页）
     * 
     * @param reqVO 查询条件
     * @return 组织树列表
     */
    @PostMapping("/tree")
    CommonResult<List<OrgRespVO>> getOrgTreeList(@Valid @RequestBody OrgListReqVO reqVO);
}

