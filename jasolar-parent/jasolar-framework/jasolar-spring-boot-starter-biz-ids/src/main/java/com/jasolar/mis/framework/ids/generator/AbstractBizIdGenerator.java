package com.jasolar.mis.framework.ids.generator;

import com.jasolar.mis.framework.ids.INegativeBizType;
import com.jasolar.mis.framework.ids.enums.ExpirationType;
import com.jasolar.mis.framework.ids.sn.SerialNumberProvider;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhahuang
 */
public abstract class AbstractBizIdGenerator implements IBizIdGenerator {

    private SerialNumberProvider serialNumberProvider;

    @Resource
    public void setRedisSerialNumberProvider(SerialNumberProvider serialNumberProvider) {
        this.serialNumberProvider = serialNumberProvider;
    }

    /**
     * 通过redis读取序号, 缓存中不存在时从数据库加载
     *
     * @param bizType
     * @param expirationType
     * @return
     */
    protected long generateSerialNumber(String bizType, ExpirationType expirationType) {
        return serialNumberProvider.generateSerialNumber(bizType, expirationType);
    }

    /**
     * @param bizType 业务类型
     * @param length  流水号长度
     * @param prefix  前缀
     * @param suffix  后缀
     */
    public String generateBizId(@Nullable String bizType, @Nullable Integer length, @Nullable String prefix, @Nullable String suffix,
                                @Nullable ExpirationType expirationType) {
        long sequence = this.generateSerialNumber(bizType, expirationType);
        int len = length == null ? 0 : length;
        String sequenceString = String.format("%0" + len + "d", sequence);
        StringBuilder bizId = new StringBuilder();
        if (StringUtils.isNotBlank(prefix)) {
            bizId.append(prefix);
        }
        bizId.append(sequenceString);
        if (StringUtils.isNotBlank(suffix)) {
            bizId.append(suffix);
        }
        return bizId.toString();
    }

    /**
     * 生成负增长的业务ID
     *
     * @param negativeBizType 负增长业务类型
     * @return 生成的负增长业务ID
     */
    @Override
    public String generateNegativeBizId(INegativeBizType negativeBizType) {
        if (negativeBizType == null) {
            return null;
        }

        // 从SerialNumberProvider获取负增长的序列号
        long currentValue = serialNumberProvider.generateNegativeSerialNumber(negativeBizType.getBizType(), negativeBizType.getOriginalValue());
        String newValue = String.valueOf(currentValue);

        // 生成新的业务ID
        StringBuilder bizId = new StringBuilder();
        if (negativeBizType.getPrefix() != null) {
            bizId.append(negativeBizType.getPrefix());
        }
        bizId.append(newValue);
        if (negativeBizType.getSuffix() != null) {
            bizId.append(negativeBizType.getSuffix());
        }

        return bizId.toString();
    }
}
