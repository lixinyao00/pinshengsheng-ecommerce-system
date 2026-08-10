package com.pinshengsheng.cart.messaging;

import com.pinshengsheng.common.event.OrderCreatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// 订单创建后异步删除购物车中已经购买的 SKU
@Component
public class CartOrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(CartOrderEventListener.class);
    private static final String CART_KEY_PREFIX = "cart:user:";

    private final StringRedisTemplate stringRedisTemplate;

    public CartOrderEventListener(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @RabbitListener(queues = CartMessagingConfig.QUEUE_NAME)
    public void handleOrderCreated(OrderCreatedMessage message) {
        if (message == null || message.getUserId() == null || message.getSkuIds() == null) {
            return;
        }

        for (Long skuId : message.getSkuIds()) {
            if (skuId != null) {
                stringRedisTemplate.opsForHash().delete(
                        CART_KEY_PREFIX + message.getUserId(),
                        String.valueOf(skuId)
                );
            }
        }

        log.info("订单消息已处理 | 订单号={} | 用户={} | SKU数量={}",
                message.getOrderNo(), message.getUserId(), message.getSkuIds().size());
    }
}
