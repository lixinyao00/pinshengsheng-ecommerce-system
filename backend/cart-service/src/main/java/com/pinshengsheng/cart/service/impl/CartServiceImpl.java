package com.pinshengsheng.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinshengsheng.cart.dto.CartAddRequest;
import com.pinshengsheng.cart.service.CartService;
import com.pinshengsheng.cart.vo.CartItemVO;
import com.pinshengsheng.cart.vo.CartSummaryVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final Duration CART_EXPIRE_DURATION = Duration.ofDays(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public CartServiceImpl(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CartSummaryVO getCart(Long userId) {
        List<CartItemVO> items = getCartItems(userId);

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
        CartItemVO item = getCartItem(userId, request.getSkuId());

        if (item == null) {
            item = new CartItemVO();
            // Redis Hash 以 skuId 作为 field，接口中的 id 也统一返回 skuId
            item.setId(request.getSkuId());
            item.setProductId(request.getProductId());
            item.setSkuId(request.getSkuId());
            item.setProductName(request.getProductName());
            item.setSkuName(request.getSkuName());
            item.setMainImage(request.getMainImage());
            item.setPrice(request.getPrice());
            item.setQuantity(request.getQuantity());
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        }

        item.setSelected(true);
        refreshSubtotal(item);
        saveCartItem(userId, item);
        refreshExpire(userId);
        return item;
    }

    @Override
    public CartItemVO updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItemVO item = getCartItem(userId, cartItemId);
        if (item == null) {
            return null;
        }

        item.setQuantity(quantity);
        refreshSubtotal(item);
        saveCartItem(userId, item);
        refreshExpire(userId);
        return item;
    }

    @Override
    public CartItemVO updateSelected(Long userId, Long cartItemId, Boolean selected) {
        CartItemVO item = getCartItem(userId, cartItemId);
        if (item == null) {
            return null;
        }

        item.setSelected(selected);
        saveCartItem(userId, item);
        refreshExpire(userId);
        return item;
    }

    @Override
    public void updateAllSelected(Long userId, Boolean selected) {
        List<CartItemVO> items = getCartItems(userId);
        Map<String, String> updates = new HashMap<>();

        for (CartItemVO item : items) {
            item.setSelected(selected);
            updates.put(String.valueOf(item.getSkuId()), serialize(item));
        }

        if (!updates.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(buildCartKey(userId), updates);
            refreshExpire(userId);
        }
    }

    @Override
    public boolean deleteItem(Long userId, Long cartItemId) {
        Long deleted = stringRedisTemplate.opsForHash().delete(
                buildCartKey(userId),
                String.valueOf(cartItemId)
        );
        return deleted != null && deleted > 0;
    }

    @Override
    public void clearCart(Long userId) {
        stringRedisTemplate.delete(buildCartKey(userId));
    }

    private List<CartItemVO> getCartItems(Long userId) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash()
                .entries(buildCartKey(userId));
        List<CartItemVO> items = new ArrayList<>();

        for (Object value : entries.values()) {
            if (value instanceof String json) {
                items.add(deserialize(json));
            }
        }

        items.sort(Comparator.comparing(CartItemVO::getId).reversed());
        return items;
    }

    private CartItemVO getCartItem(Long userId, Long skuId) {
        Object value = stringRedisTemplate.opsForHash().get(
                buildCartKey(userId),
                String.valueOf(skuId)
        );
        return value instanceof String json ? deserialize(json) : null;
    }

    private void saveCartItem(Long userId, CartItemVO item) {
        stringRedisTemplate.opsForHash().put(
                buildCartKey(userId),
                String.valueOf(item.getSkuId()),
                serialize(item)
        );
    }

    private String buildCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    private void refreshSubtotal(CartItemVO item) {
        item.setSubtotal(item.getPrice().multiply(
                BigDecimal.valueOf(item.getQuantity())
        ));
    }

    private void refreshExpire(Long userId) {
        stringRedisTemplate.expire(buildCartKey(userId), CART_EXPIRE_DURATION);
    }

    private String serialize(CartItemVO item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("购物车数据保存失败", exception);
        }
    }

    private CartItemVO deserialize(String json) {
        try {
            return objectMapper.readValue(json, CartItemVO.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("购物车数据读取失败", exception);
        }
    }
}
