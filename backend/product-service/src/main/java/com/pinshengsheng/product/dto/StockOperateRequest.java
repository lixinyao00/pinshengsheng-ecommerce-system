package com.pinshengsheng.product.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockOperateRequest {
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量至少为1")
    private Integer quantity;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
