package com.pinshengsheng.order.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.order.entity.Order;
import com.pinshengsheng.order.service.OrderService;
import com.pinshengsheng.order.vo.OrderDetailVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 管理员查看订单和处理发货
@RestController
@RequestMapping("/api/admin/order")
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Order>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Integer status) {
        if (!isAdmin(authorization)) {
            return ApiResponse.fail(403, "没有管理员权限");
        }
        return ApiResponse.success(orderService.listForAdmin(status));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        if (!isAdmin(authorization)) {
            return ApiResponse.fail(403, "没有管理员权限");
        }

        OrderDetailVO detail = orderService.getDetailForAdmin(orderId);
        return detail == null
                ? ApiResponse.fail(404, "订单不存在")
                : ApiResponse.success(detail);
    }

    @PutMapping("/{orderId}/ship")
    public ApiResponse<OrderDetailVO> ship(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        if (!isAdmin(authorization)) {
            return ApiResponse.fail(403, "没有管理员权限");
        }

        Order order = orderService.getById(orderId);
        if (order == null) {
            return ApiResponse.fail(404, "订单不存在");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            return ApiResponse.fail(409, "只有已支付订单可以发货");
        }
        if (!orderService.ship(orderId)) {
            return ApiResponse.fail(409, "订单发货失败");
        }
        return ApiResponse.success(orderService.getDetailForAdmin(orderId));
    }

    private boolean isAdmin(String authorization) {
        return "Bearer temp-token-admin".equals(authorization);
    }
}
