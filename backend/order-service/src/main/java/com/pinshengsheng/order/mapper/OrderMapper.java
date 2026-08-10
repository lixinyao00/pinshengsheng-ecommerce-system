package com.pinshengsheng.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinshengsheng.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

// 订单主表数据访问接口
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    // 自动取消前锁定订单，避免多个实例重复处理
    @Select("SELECT * FROM pss_order WHERE id = #{orderId} FOR UPDATE")
    Order selectByIdForUpdate(Long orderId);
}
