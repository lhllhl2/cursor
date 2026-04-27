package com.jasolar.mis.framework.common.exception.enums;

import com.jasolar.mis.framework.common.exception.ErrorCode;
import com.jasolar.mis.framework.common.exception.I18nedException;

/**
 * 全局错误码枚举
 * 0-999 系统异常编码保留
 * <p>
 * 一般情况下，使用 HTTP 响应状态码 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Status
 * 虽然说，HTTP 响应状态码作为业务使用表达能力偏弱，但是使用在系统层面还是非常不错的
 * 比较特殊的是，因为之前一直使用 0 作为成功，就不使用 200 啦。
 *
 * @author zhaohuang
 */
public interface GlobalErrorCodeConstants {

    // 添加默认方法或抽象方法，以符合 Checkstyle 规则
    default String getModule() {
        return "Global";
    }

    /** 全局异常的国际化前缀, 用于数字错误代码 */
    String I18B_PREFIX = "err.global.";

    ErrorCode SUCCESS = new ErrorCode("0", "成功");

    /** 一般用于抛出 {@link I18nedException} */
    ErrorCode ERROR = new ErrorCode("error", "异常");

    // ========== 客户端错误段 ==========

    GlobalErrorCode BAD_REQUEST = new GlobalErrorCode("400", "请求参数不正确");
    GlobalErrorCode UNAUTHORIZED = new GlobalErrorCode("401", "账号未登录");
    GlobalErrorCode FORBIDDEN = new GlobalErrorCode("403", "没有该操作权限");
    GlobalErrorCode NOT_FOUND = new GlobalErrorCode("404", "请求未找到");
    GlobalErrorCode METHOD_NOT_ALLOWED = new GlobalErrorCode("405", "请求方法不正确");
    GlobalErrorCode LOCKED = new GlobalErrorCode("423", "请求失败，请稍后重试"); // 并发请求，不允许
    GlobalErrorCode TOO_MANY_REQUESTS = new GlobalErrorCode("429", "请求过于频繁，请稍后重试");

    // ========== 服务端错误段 ==========

    GlobalErrorCode INTERNAL_SERVER_ERROR = new GlobalErrorCode("500", "系统异常");
    GlobalErrorCode NOT_IMPLEMENTED = new GlobalErrorCode("501", "功能未实现/未开启");
    GlobalErrorCode SERVICE_UNAVAILABLE = new GlobalErrorCode("502", "Service Unavailable");

    // ========== 自定义错误段 ==========
    String CODE_REPEATED_REQUEST = "err.request.repeated";
    ErrorCode REPEATED_REQUESTS = new ErrorCode(CODE_REPEATED_REQUEST, "重复请求，请稍后重试"); // 重复请求

    /** 系统升级发版后,会进行冒烟测试,此时普通用户不允许登录,仅允许白名单中的用户登录 */
    ErrorCode MAINTENANCE = new ErrorCode("err.system.maintenance", "系统正在维护中，请稍后再试");

    GlobalErrorCode DEMO_DENY = new GlobalErrorCode("901", "演示模式，禁止写操作");

    GlobalErrorCode UNKNOWN = new GlobalErrorCode("999", "未知错误");

    /**
     * 全局异常
     * 
     * @author galuo
     * @date 2025-06-16 10:41
     *
     */
    static class GlobalErrorCode extends ErrorCode {

        private String i18nCode;

        public GlobalErrorCode(String code, String message) {
            super(code, message);
            i18nCode = I18B_PREFIX + code;
        }

        public GlobalErrorCode(String code, String i18nCode, String message) {
            super(code, message);
            this.i18nCode = i18nCode;
        }

        public String getI18nCode() {
            return i18nCode;
        }

    }

}
