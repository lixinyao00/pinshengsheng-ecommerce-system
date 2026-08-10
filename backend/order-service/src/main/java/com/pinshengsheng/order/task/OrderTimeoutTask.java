package com.pinshengsheng.order.task;

import com.pinshengsheng.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 定时检查并取消超过支付时间的订单
@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;

    @Value("${order.timeout-minutes:30}")
    private long timeoutMinutes;

    public OrderTimeoutTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${order.timeout-check-delay-ms:60000}")
    public void cancelTimeoutOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Long> orderIds = orderService.listExpiredOrderIds(cutoff);
        int cancelledCount = 0;

        for (Long orderId : orderIds) {
            if (orderService.cancelExpiredOrder(orderId, cutoff)) {
                cancelledCount++;
            }
        }

        if (!orderIds.isEmpty()) {
            log.info("订单超时检查 | 检查={} | 自动取消={}", orderIds.size(), cancelledCount);
        }
    }
}
