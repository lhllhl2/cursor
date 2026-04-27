package com.jasolar.mis.framework.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jasolar.mis.framework.api.config.ApiScannerProperties;
import com.jasolar.mis.module.system.api.permission.dto.ApiImportResult;
import com.jasolar.mis.module.system.api.permission.dto.InterfaceResourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API接口扫描工具
 * 用于扫描系统中的所有API接口并生成接口资源数据
 */
@Slf4j
public class ApiScannerUtil implements CommandLineRunner {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final InterfaceResourceCaller resourceCaller;
    private final ApiScannerProperties properties;
    private final String serviceName;

    /**
     * 扫描到的接口资源列表
     */
    private List<InterfaceResource> interfaceResources;

    public ApiScannerUtil(
            RequestMappingHandlerMapping requestMappingHandlerMapping,
            InterfaceResourceCaller resourceCaller,
            ApiScannerProperties properties,
            @Value("${spring.application.name:unknown}") String serviceName) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.resourceCaller = resourceCaller;
        this.properties = properties;
        this.serviceName = serviceName;
    }

    @Override
    public void run(String... args) {
        if (properties.isEnabled()) {
            log.info("===============API接口扫描开始=================");
            long a = System.currentTimeMillis();
            scanApis();
            log.info("API接口扫描完成，共发现 {} 个接口", interfaceResources.size());
            a = (System.currentTimeMillis() - a) / 1000;
            log.info("===============API接口扫描完成，总共耗时{}s=================", a);

            if (properties.isAutoImport() && resourceCaller != null && resourceCaller.isAvailable()) {
                log.info("===============API接口开始自动导入=================");
                a = System.currentTimeMillis();
                ApiImportResult result = resourceCaller.importData(getInterfaceResourceList(this.interfaceResources));
                log.info(result.toString());
                a = (System.currentTimeMillis() - a) / 1000;
                log.info("===============API接口自动导入耗时{}s=================", a);
                log.info("===============API接口自动导入成功=================");
            } else if (properties.isAutoImport()) {
                log.warn("===============无法自动导入API接口：接口资源调用器不可用=================");
            }
        }
    }

    /**
     * 扫描所有API接口
     */
    public void scanApis() {
        interfaceResources = new ArrayList<>();

        // 获取所有RequestMapping
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 检查是否是Feign接口
            if (properties.isExcludeFeign() && isFeignClient(handlerMethod.getBeanType())) {
                continue;
            }

            // 处理每个接口
            processApiMethod(mappingInfo, handlerMethod);
        }
    }

    /**
     * 处理API方法并转换为接口资源对象
     */
    private void processApiMethod(RequestMappingInfo mappingInfo, HandlerMethod handlerMethod) {
        Class<?> controllerClass = handlerMethod.getBeanType();
        Method method = handlerMethod.getMethod();

        // 获取URL模式集合
        Set<String> patterns = getUrlPatterns(mappingInfo);
        if (CollUtil.isEmpty(patterns)) {
            return;
        }

        // 获取HTTP方法
        Set<RequestMethod> requestMethods = getRequestMethods(mappingInfo, method);
        if (CollUtil.isEmpty(requestMethods)) {
            return;
        }

        // 获取类别名称
        String categoryName = getCategoryName(controllerClass);

        // 获取控制器名称
        String controllerName = getControllerName(controllerClass);

        // 获取方法名称
        String functionName = method.getName();

        // 获取接口名称
        String name = getApiName(method, controllerClass);

        // 获取接口描述
        String description = getApiDescription(method, controllerClass);

        //权限key
        String permissionKey = getPermissionKey(method, controllerClass);

        // 为每个URL和HTTP方法组合创建一个接口资源对象
        for (String pattern : patterns) {
            for (RequestMethod requestMethod : requestMethods) {
                InterfaceResource resource = InterfaceResource.builder()
                        .serviceName(serviceName)
                        .categoryName(categoryName)
                        .controllerName(controllerName)
                        .functionName(functionName)
                        .name(name)
                        .url(pattern)
                        .method(requestMethod.name())
                        .description(description)
                        .permissionKey(permissionKey)
                        .status((short) 1) // 默认启用
                        .build();

                interfaceResources.add(resource);
            }
        }
    }

    /**
     * 获取URL模式集合
     */
    private Set<String> getUrlPatterns(RequestMappingInfo mappingInfo) {
        try {
            if (mappingInfo.getPathPatternsCondition() != null) {
                return mappingInfo.getPathPatternsCondition().getPatterns().stream()
                        .map(pattern -> pattern.getPatternString())
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.warn("获取URL模式异常：{}", e.getMessage());
        }
        return Collections.emptySet();
    }

    /**
     * 获取HTTP方法集合
     */
    private Set<RequestMethod> getRequestMethods(RequestMappingInfo mappingInfo, Method method) {
        // 首先尝试从HandlerMapping中获取
        if (mappingInfo.getMethodsCondition() != null && !mappingInfo.getMethodsCondition().getMethods().isEmpty()) {
            return mappingInfo.getMethodsCondition().getMethods();
        }

        // 如果没有在HandlerMapping中指定，则尝试从注解中获取
        if (AnnotatedElementUtils.hasAnnotation(method, GetMapping.class)) {
            return Set.of(RequestMethod.GET);
        } else if (AnnotatedElementUtils.hasAnnotation(method, PostMapping.class)) {
            return Set.of(RequestMethod.POST);
        } else if (AnnotatedElementUtils.hasAnnotation(method, PutMapping.class)) {
            return Set.of(RequestMethod.PUT);
        } else if (AnnotatedElementUtils.hasAnnotation(method, DeleteMapping.class)) {
            return Set.of(RequestMethod.DELETE);
        } else if (AnnotatedElementUtils.hasAnnotation(method, PatchMapping.class)) {
            return Set.of(RequestMethod.PATCH);
        }

        // 默认情况下，如果只有@RequestMapping且没有指定方法，则对所有方法有效
        return new HashSet<>(Arrays.asList(RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.PATCH));
    }

    /**
     * 获取类别名称（从Tag注解或类名）
     */
    private String getCategoryName(Class<?> controllerClass) {
        Tag tag = AnnotationUtils.findAnnotation(controllerClass, Tag.class);
        if (tag != null && StrUtil.isNotBlank(tag.name())) {
            return tag.name();
        }

        // 尝试从类名解析
        String simpleName = controllerClass.getSimpleName();
        if (simpleName.endsWith("Controller")) {
            return simpleName.substring(0, simpleName.length() - "Controller".length());
        }
        return simpleName;
    }

    /**
     * 获取控制器名称
     */
    private String getControllerName(Class<?> controllerClass) {
        return controllerClass.getName();
    }

    /**
     * 获取API名称（从Operation注解或方法名）
     */
    private String getApiName(Method method, Class<?> controllerClass) {
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null && StrUtil.isNotBlank(operation.summary())) {
            return operation.summary();
        }

        return method.getName();
    }

    /**
     * 获取API描述（从Operation注解的description）
     */
    private String getApiDescription(Method method, Class<?> controllerClass) {
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null && StrUtil.isNotBlank(operation.description())) {
            return operation.description();
        }

        return "";
    }

    /**
     * 查找权限标识
     *
     * @param method
     * @param controllerClass
     * @return
     */
    private String getPermissionKey(Method method, Class<?> controllerClass) {
        String ss = "@ss.hasPermission('";
        String es = "')";
        PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
        if (preAuthorize != null && StrUtil.isNotBlank(preAuthorize.value())) {
            String s = preAuthorize.value();
            if (s.contains(ss)) {
                int a = s.indexOf(ss) + ss.length(); // 跳过"@ss.hasPermission('"
                int b = s.indexOf(es, a); // 从a位置开始查找es
                if (b > a) {  // 确保找到了正确的结束位置
                    return s.substring(a, b); // 提取权限字符串
                }
            }
            // 如果没有找到匹配的模式，则直接返回原始值（可能不需要）
            return s;
        }
        return "";  // 如果注解不存在或为空，则返回空字符串
    }

    /**
     * 检查类是否是Feign客户端
     */
    private boolean isFeignClient(Class<?> clazz) {
        return AnnotationUtils.findAnnotation(clazz, FeignClient.class) != null;
    }

    /**
     * 获取扫描到的所有接口资源
     */
    public List<InterfaceResource> getInterfaceResources() {
        if (interfaceResources == null) {
            scanApis();
        }
        return interfaceResources;
    }

    /**
     * 手动触发扫描
     */
    public List<InterfaceResource> scan() {
        scanApis();
        return interfaceResources;
    }

    /**
     * 按类别名称分组获取接口
     */
    public Map<String, List<InterfaceResource>> getInterfaceResourcesByCategory() {
        if (interfaceResources == null) {
            scanApis();
        }
        return interfaceResources.stream()
                .collect(Collectors.groupingBy(InterfaceResource::getCategoryName));
    }


    /**
     * 转换成InterfaceResourceDTO列表
     */
    public List<InterfaceResourceDTO> getInterfaceResourceList(List<InterfaceResource> apis) {
        if (CollUtil.isNotEmpty(apis)) {
            return apis.stream().map(e -> {
                InterfaceResourceDTO item = new InterfaceResourceDTO();
                BeanUtil.copyProperties(e, item);
                return item;
            }).toList();
        }
        return null;
    }


}