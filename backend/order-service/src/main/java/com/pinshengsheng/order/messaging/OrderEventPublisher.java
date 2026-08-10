package com.pinshengsheng.order.messaging;

import com.pinshengsheng.common.event.OrderCreatedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// 把订单创建事件发送到 RabbitMQ
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreatedMessage message) {
        rabbitTemplate.convertAndSend(
                OrderMessagingConfig.EXCHANGE_NAME,
                OrderMessagingConfig.ORDER_CREATED_ROUTING_KEY,
                message
        );
    }
}
