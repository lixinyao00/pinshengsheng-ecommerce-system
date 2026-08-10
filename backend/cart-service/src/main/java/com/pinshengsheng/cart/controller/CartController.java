package com.pinshengsheng.cart.controller;

import com.pinshengsheng.cart.dto.CartAddRequest;
import com.pinshengsheng.cart.dto.CartQuantityUpdateRequest;
import com.pinshengsheng.cart.dto.CartSelectedUpdateRequest;
import com.pinshengsheng.cart.service.CartService;
import com.pinshengsheng.cart.vo.CartItemVO;
import com.pinshengsheng.cart.vo.CartSummaryVO;
import com.pinshengsheng.common.auth.TokenUtils;
import com.pinshengsheng.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<CartSummaryVO> getCart(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return ApiResponse.success(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ApiResponse<CartItemVO> addItem(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CartAddRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return ApiResponse.success(cartService.addItem(userId, request));
    }

    @PutMapping("/items/{id}/quantity")
    public ApiResponse<CartItemVO> updateQuantity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody CartQuantityUpdateRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        CartItemVO item = cartService.updateQuantity(userId, id, request.getQuantity());
        if (item == null) {
            return ApiResponse.fail(404, "购物车商品不存在");
        }
        return ApiResponse.success(item);
    }

    @PutMapping("/items/{id}/selected")
    public ApiResponse<CartItemVO> updateSelected(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody CartSelectedUpdateRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        CartItemVO item = cartService.updateSelected(userId, id, request.getSelected());
        if (item == null) {
            return ApiResponse.fail(404, "购物车商品不存在");
        }
        return ApiResponse.success(item);
    }

    @PutMapping("/selected")
    public ApiResponse<Void> updateAllSelected(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CartSelectedUpdateRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        cartService.updateAllSelected(userId, request.getSelected());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        if (!cartService.deleteItem(userId, id)) {
            return ApiResponse.fail(404, "购物车商品不存在");
        }
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        cartService.clearCart(userId);
        return ApiResponse.success(null);
    }

    private Long getUserId(String authorization) {
        return TokenUtils.getUserId(authorization);
    }
}
