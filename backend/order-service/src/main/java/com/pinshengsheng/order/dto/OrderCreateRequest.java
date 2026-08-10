package com.pinshengsheng.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 创建订单需要选择收货地址和要购买的 SKU
public class OrderCreateRequest {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemCreateRequest> items;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public List<OrderItemCreateRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemCreateRequest> items) {
        this.items = items;
    }
}
