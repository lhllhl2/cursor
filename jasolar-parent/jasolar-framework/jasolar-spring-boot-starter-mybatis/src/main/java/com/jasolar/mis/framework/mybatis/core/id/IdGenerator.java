package com.jasolar.mis.framework.mybatis.core.id;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * 用于生成主键，子类实现的时候应该满足分布式系统的需要
 * 
 * @author galuo@deloitte.com.cn
 * @date 2021-03-10 18:23
 */
public interface IdGenerator extends IdentifierGenerator {

    /**
     * 生成主键
     * 
     * @return 整数ID
     */
    long generate();

    @Override
    default Long nextId(Object entity) {
        return this.generate();
    }

    /**
     * 生成定长字符串ID。
     * 固定为19位，不足19位的数字在前面补0
     * 
     * @return 字符串ID
     */
    default String generateString() {
        return String.format("%019d", this.generate());
    }

}
