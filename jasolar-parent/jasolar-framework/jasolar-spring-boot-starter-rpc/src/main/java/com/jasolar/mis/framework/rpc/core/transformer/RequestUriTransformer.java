package com.jasolar.mis.framework.rpc.core.transformer;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSON;
import com.jasolar.mis.framework.rpc.core.transformer.ServiceUriProperties.ServiceUri;

import feign.Request;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * 拦截微服务URL, 通过配置对服务的接口地址进行修改
 */
@Slf4j
public class RequestUriTransformer implements LoadBalancerFeignRequestTransformer {

    /** 缓存微服务的配置 */
    private final Map<String, URI> caches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /** 每个微服务对应的替换地址 */
    private final Map<String, ServiceUri> uris = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /** 默认配置 */
    private final ServiceUri defaultUri;

    /**
     * 初始化
     * 
     * @param uris 拦截的服务配置
     */
    public RequestUriTransformer(List<ServiceUri> uris) {
        super();
        if (uris != null) {
            uris.forEach(u -> this.uris.put(u.getName(), u));
        }
        defaultUri = this.uris.get(ServiceUriProperties.DEFAULT);
    }

    @Override
    public Request transformRequest(Request request, ServiceInstance instance) {
        URI uri = getServiceUri(instance);
        if (uri == null) {
            return request;
        }
        return this.buildRequest(request, uri);
    }

    /**
     * 使用新的服务地址替换原始请求
     * 
     * @param request 原始请求
     * @param instance 需要替换成的微服务地址
     * @return 替换服务地址后的新请求
     */
    protected Request buildRequest(Request request, URI instance) {
        URI uri = doReconstructURI(instance, URI.create(request.url()));
        String reconstructedUrl = uri.toString();
        log.info(" request: {} -> {}", request.url(), reconstructedUrl);
        return Request.create(request.httpMethod(), reconstructedUrl, request.headers(), request.body(), request.charset(),
                request.requestTemplate());
    }

    /**
     * 根据微服务实例对象获取URI配置
     * 
     * @param instance 微服务实例参数
     * @return 替换的服务地址,为null则表示无需替换
     */
    @Nullable
    private URI getServiceUri(ServiceInstance instance) {
        if (uris.isEmpty()) {
            return null;
        }
        String name = instance.getServiceId();
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (caches.containsKey(name)) {
            return caches.get(name);
        }

        URI u = null;
        ServiceUri prop = uris.getOrDefault(name, defaultUri);
        if (prop != null) {
            String uri = prop.getUri();
            if (StringUtils.isNotBlank(uri) && !ServiceUriProperties.NONE.equalsIgnoreCase(uri)) {
                // 修改为配置的地址
                u = URI.create(uri);
                log.info("微服务地址替换: {} -> {}", JSON.toJSONString(instance), u);
            }
        }
        caches.put(name, u);
        return u;
    }

    /**
     * 重建请求的URL
     * 
     * @param serviceUri 微服务配置的地址,需要将真实地址替换掉
     * @param original 微服务从loadbalancer取到的真实地址
     * @return
     */
    private static URI doReconstructURI(URI serviceUri, URI original) {
        String host = serviceUri.getHost();
        String scheme = serviceUri.getScheme();
        int port = serviceUri.getPort();
        if (Objects.equals(host, original.getHost()) && port == original.getPort() && Objects.equals(scheme, original.getScheme())) {
            return original;
        }

        UriComponentsBuilder uri = UriComponentsBuilder.fromUri(original).scheme(scheme).host(host).port(port);
        if (StringUtils.isNotBlank(serviceUri.getRawPath()) && !"/".equals(serviceUri.getRawPath())) {
            uri.replacePath(serviceUri.getRawPath()).path(original.getRawPath());
        }

        boolean encoded = containsEncodedParts(original);
        return uri.build(encoded).toUri();
    }

    private static final String PERCENTAGE_SIGN = "%";

    // see original
    // https://github.com/spring-cloud/spring-cloud-gateway/blob/main/spring-cloud-gateway-core/
    // src/main/java/org/springframework/cloud/gateway/support/ServerWebExchangeUtils.java
    private static boolean containsEncodedParts(URI uri) {
        boolean encoded = (uri.getRawQuery() != null && uri.getRawQuery().contains(PERCENTAGE_SIGN))
                || (uri.getRawPath() != null && uri.getRawPath().contains(PERCENTAGE_SIGN))
                || (uri.getRawFragment() != null && uri.getRawFragment().contains(PERCENTAGE_SIGN));
        // Verify if it is really fully encoded. Treat partial encoded as unencoded.
        if (encoded) {
            try {
                UriComponentsBuilder.fromUri(uri).build(true);
                return true;
            } catch (IllegalArgumentException ignore) {
                log.error("error", ignore);
            }
            return false;
        }
        return false;
    }
}
