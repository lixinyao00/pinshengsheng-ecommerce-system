package com.pinshengsheng.cart.service;

import com.pinshengsheng.cart.dto.CartAddRequest;
import com.pinshengsheng.cart.vo.CartItemVO;
import com.pinshengsheng.cart.vo.CartSummaryVO;

public interface CartService {

    CartSummaryVO getCart(Long userId);
    CartItemVO addItem(Long userId, CartAddRequest request);
    CartItemVO updateQuantity(Long userId, Long cartItemId, Integer quantity);
    CartItemVO updateSelected(Long userId, Long cartItemId, Boolean selected);
    void updateAllSelected(Long userId, Boolean selected);
    boolean deleteItem(Long userId, Long cartItemId);
}
