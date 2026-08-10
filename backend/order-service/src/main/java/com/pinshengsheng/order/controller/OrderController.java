package com.pinshengsheng.order.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.order.dto.OrderCreateRequest;
import com.pinshengsheng.order.entity.Order;
import com.pinshengsheng.order.service.OrderService;
import com.pinshengsheng.order.vo.OrderDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetailVO> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody OrderCreateRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        OrderDetailVO detail = orderService.create(userId, request);
        return detail == null
                ? ApiResponse.fail(400, "商品不存在、已下架、库存不足或收货地址不存在")
                : ApiResponse.success(detail);
    }

    @GetMapping("/list")
    public ApiResponse<List<Order>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return ApiResponse.success(orderService.listByUserId(userId));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailVO> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        OrderDetailVO detail = orderService.getDetail(userId, orderId);
        return detail == null
                ? ApiResponse.fail(404, "订单不存在")
                : ApiResponse.success(detail);
    }

    @PutMapping("/{orderId}/pay")
    public ApiResponse<OrderDetailVO> pay(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        Order order = orderService.getByUserId(userId, orderId);
        if (order == null) {
            return ApiResponse.fail(404, "订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            return ApiResponse.fail(409, "当前订单不能支付");
        }
        if (!orderService.pay(userId, orderId)) {
            return ApiResponse.fail(409, "订单支付失败");
        }
        return ApiResponse.success(orderService.getDetail(userId, orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<OrderDetailVO> cancel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        Order order = orderService.getByUserId(userId, orderId);
        if (order == null) {
            return ApiResponse.fail(404, "订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            return ApiResponse.fail(409, "当前订单不能取消");
        }
        if (!orderService.cancel(userId, orderId)) {
            return ApiResponse.fail(409, "库存释放失败，订单暂时不能取消");
        }
        return ApiResponse.success(orderService.getDetail(userId, orderId));
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
        return ApiResponse.success(orderService.getDetail(order.getUserId(), orderId));
    }

    @PutMapping("/{orderId}/complete")
    public ApiResponse<OrderDetailVO> complete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }

        Order order = orderService.getByUserId(userId, orderId);
        if (order == null) {
            return ApiResponse.fail(404, "订单不存在");
        }
        if (!Integer.valueOf(2).equals(order.getStatus())) {
            return ApiResponse.fail(409, "只有已发货订单可以确认收货");
        }
        if (!orderService.complete(userId, orderId)) {
            return ApiResponse.fail(409, "确认收货失败");
        }
        return ApiResponse.success(orderService.getDetail(userId, orderId));
    }

    private Long getUserId(String authorization) {
        if ("Bearer temp-token-user".equals(authorization)) {
            return 1L;
        }
        return null;
    }

    private boolean isAdmin(String authorization) {
        return "Bearer temp-token-admin".equals(authorization);
    }
}
