package com.pinshengsheng.product.service.impl;

import com.pinshengsheng.product.service.LuaStockService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class LuaStockServiceImpl implements LuaStockService {

    private static final String STOCK_KEY_PREFIX = "demo:stock:sku:";

    // Lua 脚本把“查库存”和“扣库存”放到 Redis 内一次执行
    private static final DefaultRedisScript<Long> DEDUCT_STOCK_SCRIPT =
            new DefaultRedisScript<>(
                    "local stock = tonumber(redis.call('GET', KEYS[1])) " +
                            "local amount = tonumber(ARGV[1]) " +
                            "if not stock then return -1 end " +
                            "if not amount or amount <= 0 then return -2 end " +
                            "if stock < amount then return 0 end " +
                            "redis.call('DECRBY', KEYS[1], amount) " +
                            "return 1",
                    Long.class
            );

    private final StringRedisTemplate stringRedisTemplate;

    public LuaStockServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void initStock(Long skuId, Integer quantity) {
        validateSkuId(skuId);
        validateQuantity(quantity);

        // 初始化演示库存，使用独立 key，不影响 MySQL 正式库存
        stringRedisTemplate.opsForValue().set(buildStockKey(skuId), quantity.toString());
    }

    @Override
    public Long getStock(Long skuId) {
        validateSkuId(skuId);

        String stock = stringRedisTemplate.opsForValue().get(buildStockKey(skuId));
        return stock == null ? null : Long.valueOf(stock);
    }

    @Override
    public boolean deductStock(Long skuId, Integer quantity) {
        validateSkuId(skuId);
        validateQuantity(quantity);

        // execute 会把 key 和购买数量传给 Redis 中的 Lua 脚本
        Long result = stringRedisTemplate.execute(
                DEDUCT_STOCK_SCRIPT,
                Collections.singletonList(buildStockKey(skuId)),
                quantity.toString()
        );

        return Long.valueOf(1).equals(result);
    }

    private String buildStockKey(Long skuId) {
        return STOCK_KEY_PREFIX + skuId;
    }

    private void validateSkuId(Long skuId) {
        if (skuId == null || skuId <= 0) {
            throw new IllegalArgumentException("skuId 必须大于0");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("库存数量必须大于0");
        }
    }
}
