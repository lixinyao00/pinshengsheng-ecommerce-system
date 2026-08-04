package com.pinshengsheng.product.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.vo.ProductDetailVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDetailCacheServiceTest {

    private static final String CACHE_KEY = "pss:product:detail:901";

    private final LettuceConnectionFactory connectionFactory =
            new LettuceConnectionFactory("127.0.0.1", 6379);
    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();
    private final ProductDetailCacheService cacheService;
    private final StringRedisTemplate stringRedisTemplate;

    ProductDetailCacheServiceTest() {
        connectionFactory.afterPropertiesSet();

        stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(connectionFactory);
        stringRedisTemplate.afterPropertiesSet();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        cacheService = new ProductDetailCacheService(
                stringRedisTemplate,
                objectMapper,
                executorService
        );
    }

    @AfterEach
    void tearDown() {
        stringRedisTemplate.delete(CACHE_KEY);
        executorService.shutdownNow();
        connectionFactory.destroy();
    }

    @Test
    void shouldWriteAndReadProductDetail() {
        ProductDetailVO detailVO = new ProductDetailVO();
        Product product = new Product();
        product.setId(901L);
        product.setName("缓存测试商品");
        product.setMinPrice(new BigDecimal("88.00"));
        product.setCreateTime(LocalDateTime.now());
        detailVO.setProduct(product);

        cacheService.writeProductDetailCache(901L, detailVO);
        ProductDetailCacheService.CacheLookupResult result =
                cacheService.readProductDetailCache(901L);

        assertTrue(result.isHit());
        assertFalse(result.isEmpty());
        assertEquals("缓存测试商品", result.getDetailVO().getProduct().getName());
    }

    @Test
    void shouldWriteAndReadEmptyCache() {
        cacheService.writeEmptyCache(901L);

        ProductDetailCacheService.CacheLookupResult result =
                cacheService.readProductDetailCache(901L);

        assertTrue(result.isHit());
        assertTrue(result.isEmpty());
    }
}
