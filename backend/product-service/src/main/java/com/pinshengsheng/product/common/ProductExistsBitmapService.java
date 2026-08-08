package com.pinshengsheng.product.common;

import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.mapper.ProductMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductExistsBitmapService {

    private static final String BITMAP_KEY = "product:exists:bitmap";
    private static final String READY_KEY = "product:exists:bitmap:ready";

    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public ProductExistsBitmapService(
            ProductMapper productMapper,
            StringRedisTemplate stringRedisTemplate) {
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void rebuildBitmap() {
        try {
            // 先删除就绪标记，重建期间请求会降级到数据库查询
            stringRedisTemplate.delete(READY_KEY);
            stringRedisTemplate.delete(BITMAP_KEY);

            List<Product> productList = productMapper.selectList(null);
            for (Product product : productList) {
                markExists(product.getId());
            }

            stringRedisTemplate.opsForValue().set(READY_KEY, "1");
        } catch (Exception exception) {
            // Redis 不可用时不启用拦截，避免影响正常商品详情查询
        }
    }

    public boolean mayExist(Long productId) {
        if (productId == null || productId <= 0) {
            return false;
        }

        try {
            String ready = stringRedisTemplate.opsForValue().get(READY_KEY);
            if (!"1".equals(ready)) {
                return true;
            }

            Boolean exists = stringRedisTemplate.opsForValue()
                    .getBit(BITMAP_KEY, productId);
            return Boolean.TRUE.equals(exists);
        } catch (Exception exception) {
            return true;
        }
    }

    public void markExists(Long productId) {
        if (productId == null || productId <= 0) {
            return;
        }

        try {
            // 商品 ID 直接作为 Bitmap 下标，值为 1 代表数据库中存在该商品
            stringRedisTemplate.opsForValue().setBit(BITMAP_KEY, productId, true);
        } catch (Exception exception) {
            // Redis 写入失败不影响商品新增，服务重启时会自动重新构建 Bitmap
        }
    }
}
