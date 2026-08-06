package com.pinshengsheng.cart.dto;

import jakarta.validation.constraints.NotNull;

public class CartSelectedUpdateRequest {

    @NotNull(message = "选中状态不能为空")
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
}
