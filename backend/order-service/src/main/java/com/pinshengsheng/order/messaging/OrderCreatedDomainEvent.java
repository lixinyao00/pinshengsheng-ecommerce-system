package com.pinshengsheng.order.messaging;

import java.util.List;

// 订单事务提交后发送给消息发布器的内部事件
public record OrderCreatedDomainEvent(
        Long orderId,
        String orderNo,
        Long userId,
        List<Long> skuIds) {
}
