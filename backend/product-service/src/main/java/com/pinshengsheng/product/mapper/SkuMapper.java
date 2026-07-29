package com.pinshengsheng.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinshengsheng.product.entity.Sku;
import org.apache.ibatis.annotations.Mapper;

// SKU 数据访问接口
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
}
