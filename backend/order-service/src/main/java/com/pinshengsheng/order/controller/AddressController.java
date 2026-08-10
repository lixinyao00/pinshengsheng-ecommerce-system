package com.pinshengsheng.order.controller;

import com.pinshengsheng.common.api.ApiResponse;
import com.pinshengsheng.common.auth.TokenUtils;
import com.pinshengsheng.order.dto.AddressSaveRequest;
import com.pinshengsheng.order.entity.Address;
import com.pinshengsheng.order.service.AddressService;
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

import java.util.List;

@RestController
@RequestMapping("/api/order/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Address>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return ApiResponse.success(addressService.listByUserId(userId));
    }

    @PostMapping
    public ApiResponse<Address> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody AddressSaveRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return ApiResponse.success(addressService.create(userId, request));
    }

    @PutMapping("/{addressId}")
    public ApiResponse<Address> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressSaveRequest request) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        Address address = addressService.update(userId, addressId, request);
        return address == null
                ? ApiResponse.fail(404, "收货地址不存在")
                : ApiResponse.success(address);
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long addressId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return addressService.delete(userId, addressId)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "收货地址不存在");
    }

    @PutMapping("/{addressId}/default")
    public ApiResponse<Void> setDefault(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long addressId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return ApiResponse.fail(401, "登录状态已失效");
        }
        return addressService.setDefault(userId, addressId)
                ? ApiResponse.success(null)
                : ApiResponse.fail(404, "收货地址不存在");
    }

    private Long getUserId(String authorization) {
        return TokenUtils.getUserId(authorization);
    }
}
