package com.pinshengsheng.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CartQuantityUpdateRequest {

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量至少为 1")
    private Integer quantity;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
