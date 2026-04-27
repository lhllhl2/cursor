package com.jasolar.mis.framework.common.validation.group;

import jakarta.validation.groups.Default;

/**
 * 
 * 仅用于暂存时校验. 提交不做校验.
 * 暂存时大部分数据都可以为空,因此在暂存时仅作数据长度等校验. 大部分情况下, 在参数上可以不加Save的group, 直接通过Default校验即可
 * 
 * @author galuo
 * @date 2025-03-26 10:23
 *
 */
public interface Save extends Default {

}
