package com.jasolar.mis.framework.ids.generator;

import com.jasolar.mis.framework.ids.INegativeBizType;
import com.jasolar.mis.framework.ids.enums.ExpirationType;
import jakarta.annotation.Nullable;

/**
 * @author zhahuang
 */
public interface IBizIdGenerator {

    /**
     * 生成业务ID
     *
     * @param bizType 业务类型
     * @param length  业务ID长度
     * @param prefix  单据前缀
     * @param suffix  单据后缀
     */
    String generateBizId(@Nullable String bizType, @Nullable Integer length, @Nullable String prefix, @Nullable String suffix,@Nullable ExpirationType expirationType);


    /**
     * 生成负增长的业务ID
     *
     * @param negativeBizType 负增长业务类型
     * @return 生成的负增长业务ID
     */
    String generateNegativeBizId(@Nullable INegativeBizType negativeBizType);
}
