package com.pinshengsheng.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pinshengsheng.cart.dto.CartAddRequest;
import com.pinshengsheng.cart.entity.CartItem;
import com.pinshengsheng.cart.mapper.CartItemMapper;
import com.pinshengsheng.cart.service.CartService;
import com.pinshengsheng.cart.vo.CartItemVO;
import com.pinshengsheng.cart.vo.CartSummaryVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;

    public CartServiceImpl(CartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    @Override
    public CartSummaryVO getCart(Long userId) {
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getId)
        );
        List<CartItemVO> items = cartItems.stream().map(this::toCartItemVO).toList();

        int selectedCount = items.stream()
                .filter(CartItemVO::getSelected)
                .mapToInt(CartItemVO::getQuantity)
                .sum();
        BigDecimal selectedTotalAmount = items.stream()
                .filter(CartItemVO::getSelected)
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartSummaryVO summary = new CartSummaryVO();
        summary.setItems(items);
        summary.setSelectedCount(selectedCount);
        summary.setSelectedTotalAmount(selectedTotalAmount);
        return summary;
    }

    @Override
    public CartItemVO addItem(Long userId, CartAddRequest request) {
        CartItem existingItem = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, request.getSkuId())
        );
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setSelected(1);
            cartItemMapper.updateById(existingItem);
            return toCartItemVO(existingItem);
        }

        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(request.getProductId());
        cartItem.setSkuId(request.getSkuId());
        cartItem.setProductName(request.getProductName());
        cartItem.setSkuName(request.getSkuName());
        cartItem.setMainImage(request.getMainImage());
        cartItem.setPrice(request.getPrice());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSelected(1);
        cartItemMapper.insert(cartItem);
        return toCartItemVO(cartItem);
    }

    @Override
    public CartItemVO updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = getUserCartItem(userId, cartItemId);
        if (cartItem == null) {
            return null;
        }
        cartItem.setQuantity(quantity);
        cartItemMapper.updateById(cartItem);
        return toCartItemVO(cartItem);
    }

    @Override
    public CartItemVO updateSelected(Long userId, Long cartItemId, Boolean selected) {
        CartItem cartItem = getUserCartItem(userId, cartItemId);
        if (cartItem == null) {
            return null;
        }
        cartItem.setSelected(selected ? 1 : 0);
        cartItemMapper.updateById(cartItem);
        return toCartItemVO(cartItem);
    }

    @Override
    public void updateAllSelected(Long userId, Boolean selected) {
        cartItemMapper.update(null, new LambdaUpdateWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .set(CartItem::getSelected, selected ? 1 : 0));
    }

    @Override
    public boolean deleteItem(Long userId, Long cartItemId) {
        return cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, cartItemId)
                .eq(CartItem::getUserId, userId)) > 0;
    }

    private CartItem getUserCartItem(Long userId, Long cartItemId) {
        return cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getId, cartItemId)
                .eq(CartItem::getUserId, userId));
    }

    private CartItemVO toCartItemVO(CartItem cartItem) {
        CartItemVO vo = new CartItemVO();
        vo.setId(cartItem.getId());
        vo.setProductId(cartItem.getProductId());
        vo.setSkuId(cartItem.getSkuId());
        vo.setProductName(cartItem.getProductName());
        vo.setSkuName(cartItem.getSkuName());
        vo.setMainImage(cartItem.getMainImage());
        vo.setPrice(cartItem.getPrice());
        vo.setQuantity(cartItem.getQuantity());
        vo.setSelected(Integer.valueOf(1).equals(cartItem.getSelected()));
        vo.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return vo;
    }
}
