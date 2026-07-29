package com.pinshengsheng.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinshengsheng.product.entity.SkuStock;
import org.apache.ibatis.annotations.Mapper;

// 库存数据访问接口
@Mapper
public interface SkuStockMapper extends BaseMapper<SkuStock> {
}
