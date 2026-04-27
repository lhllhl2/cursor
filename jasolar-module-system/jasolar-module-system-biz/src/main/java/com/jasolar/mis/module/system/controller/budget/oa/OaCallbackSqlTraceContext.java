package com.jasolar.mis.module.system.controller.budget.oa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OA回调SQL采集上下文
 */
public final class OaCallbackSqlTraceContext {

    private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<List<String>> SQL_LIST = ThreadLocal.withInitial(ArrayList::new);

    private OaCallbackSqlTraceContext() {
    }

    public static void start() {
        ENABLED.set(Boolean.TRUE);
        SQL_LIST.get().clear();
    }

    public static boolean isEnabled() {
        return Boolean.TRUE.equals(ENABLED.get());
    }

    public static void addSql(String sql) {
        if (!isEnabled() || sql == null || sql.trim().isEmpty()) {
            return;
        }
        SQL_LIST.get().add(sql.trim());
    }

    public static List<String> getSqlList() {
        return Collections.unmodifiableList(new ArrayList<>(SQL_LIST.get()));
    }

    public static void clear() {
        ENABLED.remove();
        SQL_LIST.remove();
    }
}
