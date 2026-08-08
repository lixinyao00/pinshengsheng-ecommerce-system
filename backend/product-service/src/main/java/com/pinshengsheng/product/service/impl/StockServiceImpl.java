package com.pinshengsheng.product.service.impl;

import com.pinshengsheng.product.entity.SkuStock;
import com.pinshengsheng.product.mapper.SkuStockMapper;
import com.pinshengsheng.product.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {

    private final RedissonClient redissonClient;
    private final SkuStockMapper skuStockMapper;

    public StockServiceImpl(RedissonClient redissonClient,
                            SkuStockMapper skuStockMapper) {
        this.redissonClient = redissonClient;
        this.skuStockMapper = skuStockMapper;
    }

    @Override
    @Transactional
    public boolean lockStock(Long skuId, Integer quantity) {
        if (skuId == null || quantity == null || quantity <= 0) {
            return false;
        }

        RLock lock = redissonClient.getLock(
                "lock:stock:sku:" + skuId
        );

        boolean locked = false;

        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);

            if (!locked) {
                return false;
            }

            SkuStock stock = getStock(skuId);

            if (stock == null
                    || stock.getAvailableStock() < quantity) {
                return false;
            }

            int lockedStock = stock.getLockedStock() == null
                    ? 0
                    : stock.getLockedStock();

            stock.setAvailableStock(
                    stock.getAvailableStock() - quantity
            );
            stock.setLockedStock(
                    lockedStock + quantity
            );

            return skuStockMapper.updateById(stock) > 0;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    private SkuStock getStock(Long skuId) {
        LambdaQueryWrapper<SkuStock> queryWrapper =
                new LambdaQueryWrapper<>();

        queryWrapper.eq(SkuStock::getSkuId, skuId);

        return skuStockMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean releaseStock(Long skuId, Integer quantity) {
        if (skuId == null || quantity == null || quantity <= 0) {
            return false;
        }

        RLock lock = redissonClient.getLock(
                "lock:stock:sku:" + skuId
        );

        boolean locked = false;

        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);

            if (!locked) {
                return false;
            }

            SkuStock stock = getStock(skuId);

            if (stock == null) {
                return false;
            }

            int lockedStock = stock.getLockedStock() == null
                    ? 0
                    : stock.getLockedStock();

            if (lockedStock < quantity) {
                return false;
            }

            stock.setLockedStock(
                    lockedStock - quantity
            );
            stock.setAvailableStock(
                    stock.getAvailableStock() + quantity
            );

            return skuStockMapper.updateById(stock) > 0;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
