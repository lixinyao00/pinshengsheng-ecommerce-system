package com.pinshengsheng.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartSummaryVO {

    private List<CartItemVO> items;
    private Integer selectedCount;
    private BigDecimal selectedTotalAmount;

    public List<CartItemVO> getItems() { return items; }
    public void setItems(List<CartItemVO> items) { this.items = items; }
    public Integer getSelectedCount() { return selectedCount; }
    public void setSelectedCount(Integer selectedCount) { this.selectedCount = selectedCount; }
    public BigDecimal getSelectedTotalAmount() { return selectedTotalAmount; }
    public void setSelectedTotalAmount(BigDecimal selectedTotalAmount) { this.selectedTotalAmount = selectedTotalAmount; }
}
