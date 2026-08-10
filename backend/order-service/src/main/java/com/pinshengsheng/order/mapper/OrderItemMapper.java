package com.pinshengsheng.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinshengsheng.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

// 订单明细数据访问接口
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
