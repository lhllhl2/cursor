package com.jasolar.mis.framework.excel.core.function;

/**
 * 自定义下拉列表调用方式的参数
 * 
 * @author galuo
 * @date 2025-04-28 15:23
 *
 */
public class ExcelColumnSelectFunctionArgs {

    /** 当前线程中使用的参数 */
    private static final ThreadLocal<Object> ARGS = new InheritableThreadLocal<>();

    /**
     * 为下拉列表类添加参数
     * 
     * @param args
     */
    public static void setArgs(Object args) {
        ARGS.set(args);
    }

    /**
     * 删除参数
     * 
     */
    public static void removeArgs() {
        ARGS.remove();
    }

    /**
     * 获取当前线程中的参数
     * 
     * @return
     */
    public static Object getArgs() {
        return ARGS.get();
    }

}
