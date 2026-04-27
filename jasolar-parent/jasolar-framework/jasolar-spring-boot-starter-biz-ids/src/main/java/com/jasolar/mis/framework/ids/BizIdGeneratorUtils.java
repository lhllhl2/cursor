package com.jasolar.mis.framework.ids;

import com.jasolar.mis.framework.common.util.spring.SpringUtils;
import com.jasolar.mis.framework.ids.generator.IBizIdGenerator;
import jakarta.annotation.Nullable;

/**
 * @author zhahuang
 */
public class BizIdGeneratorUtils {

    /**
     * 根据接口实现类生成序列化ID
     *
     * @param bizType 业务类型接口实现类
     */
    public static String generateBizId(IBizType bizType) {
        if (null == bizType) {
            return null;
        }

        IBizIdGenerator bizIdGenerator = SpringUtils.getBean(IBizIdGenerator.class);
        if (null != bizIdGenerator) {
            return bizIdGenerator.generateBizId(bizType.getBizType(), bizType.getSequenceLength(), bizType.getPrefix(), bizType.getSuffix(),
                    bizType.getExpirationType());
        }
        return null;
    }

    /**
     * 生成负增长的业务ID
     *
     * @param negativeBizType 负增长业务类型接口实现类
     * @return 生成的负增长业务ID
     */
    public static String generateNegativeBizId(@Nullable INegativeBizType negativeBizType) {
        if (negativeBizType == null) {
            return null;
        }
        IBizIdGenerator bizIdGenerator = SpringUtils.getBean(IBizIdGenerator.class);
        if (null != bizIdGenerator) {
            return bizIdGenerator.generateNegativeBizId(negativeBizType);
        }
        return null;
    }
    //
    // /**
    // * 完全手动挡生成序列化ID
    // * </p>
    // * 不推荐使用此方法
    // *
    // * @param bizType 业务类型
    // * @param length 业务ID长度
    // * @param prefix 单据前缀
    // * @param suffix 单据后缀
    // */
    // public static String generateBizId(@Nullable String bizType, @Nullable Integer length, @Nullable String prefix, @Nullable String
    // suffix,@Nullable ExpirationType expirationType) {
    // if (null == bizType) {
    // return null;
    // }
    // IBizIdGenerator bizIdGenerator = SpringUtils.getBean(com.fiifoxconn.mis.framework.ids.generator.DefaultGenerator.class);
    // if (null != bizIdGenerator) {
    // return bizIdGenerator.generateBizId(bizType, length, prefix, suffix,expirationType);
    // }
    // return null;
    // }
}
