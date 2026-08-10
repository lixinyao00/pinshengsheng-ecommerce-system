package com.pinshengsheng.order.messaging;

import com.pinshengsheng.common.event.OrderCreatedMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 只有订单事务提交成功后，才向 RabbitMQ 发布订单消息
@Component
public class OrderCreatedEventListener {

    private final OrderEventPublisher orderEventPublisher;

    public OrderCreatedEventListener(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedDomainEvent event) {
        OrderCreatedMessage message = new OrderCreatedMessage();
        message.setOrderId(event.orderId());
        message.setOrderNo(event.orderNo());
        message.setUserId(event.userId());
        message.setSkuIds(event.skuIds());
        orderEventPublisher.publishOrderCreated(message);
    }
}
