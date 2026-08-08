package com.pinshengsheng.product.common;
import com.pinshengsheng.product.vo.ProductDetailVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Service
public class ProductDetailCacheService {
    // Redis 操作模板
    private final RedisTemplate<String, ProductDetailVO> redisTemplate;
    // 商品详情缓存时间
    private static final Duration CACHE_DURATION =
            Duration.ofSeconds(30);
    public ProductDetailCacheService(
            RedisTemplate<String, ProductDetailVO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private String buildKey(Long productId) {
        return "product:detail:" + productId;
    }
    public ProductDetailVO get(Long productId) {
        try {
            return redisTemplate.opsForValue().get(buildKey(productId));
        } catch (Exception exception) {
            // Redis 异常时跳过缓存，不影响商品详情查询
            return null;
        }
    }
    // 保存商品详情缓存
    public void set(Long productId, ProductDetailVO detail) {
        try {
            redisTemplate.opsForValue().set(
                    buildKey(productId),
                    detail,
                    CACHE_DURATION
            );
        } catch (Exception exception) {
            // Redis 异常时跳过缓存写入
        }
    }
    // 删除商品详情缓存
    public void delete(Long productId) {
        try {
            redisTemplate.delete(buildKey(productId));
        } catch (Exception exception) {
            // Redis 异常时跳过缓存删除
        }
    }

}
