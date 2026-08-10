package com.pinshengsheng.order.service;

import com.pinshengsheng.order.dto.OrderCreateRequest;
import com.pinshengsheng.order.entity.Order;
import com.pinshengsheng.order.vo.OrderDetailVO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderDetailVO create(Long userId, OrderCreateRequest request);

    List<Order> listByUserId(Long userId);

    OrderDetailVO getDetail(Long userId, Long orderId);

    Order getByUserId(Long userId, Long orderId);

    Order getById(Long orderId);

    boolean pay(Long userId, Long orderId);

    boolean cancel(Long userId, Long orderId);

    boolean ship(Long orderId);

    boolean complete(Long userId, Long orderId);

    List<Long> listExpiredOrderIds(LocalDateTime cutoff);

    boolean cancelExpiredOrder(Long orderId, LocalDateTime cutoff);

    List<Order> listForAdmin(Integer status);

    OrderDetailVO getDetailForAdmin(Long orderId);
}
