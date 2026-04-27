package com.jasolar.mis.bizapi;

import com.jasolar.mis.framework.common.enums.RpcConstants;

/**
 * API常量. 为了防止parent依赖下级模块, 将人员/部门/法人/字典/物料等主数据的API定义到parent中
 * 
 * @author galuo
 * @date 2025-04-08 21:06
 *
 */
public interface Apis {

    /** 主数据服务名 */
    String MASTERDATA = "masterdata-service";
    /** 主数据服务的前缀 */
    String MASTERDATA_PREFIX = RpcConstants.RPC_API_PREFIX + "/masterdata";

    /** system服务前缀 */
    String SYSTEM = "system-service";
    /** system服务名 */
    String SYSTEM_PREFIX = RpcConstants.RPC_API_PREFIX + "/system";

    /** log服务前缀 */
    String LOG = "log-service";
    /** log服务名 */
    String LOG_PREFIX = RpcConstants.RPC_API_PREFIX + "/log";

    /** infra服务前缀 */
    String INFRA = "infra-service";
    /** infra服务名 */
    String INFRA_PREFIX = RpcConstants.RPC_API_PREFIX + "/infra";

    /** bpm服务前缀 */
    String BPM = "bpm-service";
    /** bpm服务名 */
    String BPM_PREFIX = RpcConstants.RPC_API_PREFIX + "/bpm";

    /** supplier服务前缀 */
    String SUPPLIER = "supplier-service";
    /** supplier服务名 */
    String SUPPLIER_PREFIX = RpcConstants.RPC_API_PREFIX + "/supplier";

}
