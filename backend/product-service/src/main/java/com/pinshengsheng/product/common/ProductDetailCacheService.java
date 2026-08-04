package com.pinshengsheng.product.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinshengsheng.product.vo.ProductDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ProductDetailCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProductDetailCacheService.class);
    private static final String PRODUCT_DETAIL_KEY_PREFIX = "pss:product:detail:";
    private static final String EMPTY_VALUE = "EMPTY";
    private static final Duration DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration EMPTY_TTL = Duration.ofMinutes(2);
    private static final int MAX_RANDOM_MINUTES = 10;
    private static final long SECOND_DELETE_DELAY_MILLIS = 500L;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService productCacheExecutor;
    private final Random random = new Random();

    public ProductDetailCacheService(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            ScheduledExecutorService productCacheExecutor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.productCacheExecutor = productCacheExecutor;
    }

    public CacheLookupResult readProductDetailCache(Long productId) {
        String cacheKey = buildProductDetailKey(productId);
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);

        if (cacheValue == null) {
            log.info("商品详情缓存未命中，productId={}", productId);
            return CacheLookupResult.miss();
        }

        if (EMPTY_VALUE.equals(cacheValue)) {
            log.info("商品详情命中空缓存，productId={}", productId);
            return CacheLookupResult.empty();
        }

        try {
            ProductDetailVO detailVO = objectMapper.readValue(cacheValue, ProductDetailVO.class);
            log.info("商品详情命中 Redis，productId={}", productId);
            return CacheLookupResult.hit(detailVO);
        } catch (JsonProcessingException exception) {
            log.warn("商品详情缓存反序列化失败，准备删除脏数据，productId={}", productId, exception);
            stringRedisTemplate.delete(cacheKey);
            return CacheLookupResult.miss();
        }
    }

    public void writeProductDetailCache(Long productId, ProductDetailVO detailVO) {
        try {
            String cacheValue = objectMapper.writeValueAsString(detailVO);
            long randomMinutes = random.nextInt(MAX_RANDOM_MINUTES + 1);
            Duration expireTime = DETAIL_TTL.plusMinutes(randomMinutes);
            stringRedisTemplate.opsForValue().set(
                    buildProductDetailKey(productId),
                    cacheValue,
                    expireTime
            );
            log.info("商品详情写入 Redis，productId={}, expireMinutes={}", productId, expireTime.toMinutes());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("商品详情缓存写入失败", exception);
        }
    }

    public void writeEmptyCache(Long productId) {
        stringRedisTemplate.opsForValue().set(
                buildProductDetailKey(productId),
                EMPTY_VALUE,
                EMPTY_TTL
        );
        log.info("商品详情写入空缓存，productId={}", productId);
    }

    public void deleteProductDetailCache(Long productId) {
        stringRedisTemplate.delete(buildProductDetailKey(productId));
        log.info("删除商品详情缓存，productId={}", productId);
    }

    public void deleteProductDetailCacheWithDoubleDelete(Long productId) {
        deleteProductDetailCache(productId);
        productCacheExecutor.schedule(
                () -> deleteProductDetailCache(productId),
                SECOND_DELETE_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
        );
        log.info("已安排商品详情延时双删，productId={}", productId);
    }

    private String buildProductDetailKey(Long productId) {
        return PRODUCT_DETAIL_KEY_PREFIX + productId;
    }

    public static class CacheLookupResult {

        private final boolean hit;
        private final boolean empty;
        private final ProductDetailVO detailVO;

        private CacheLookupResult(boolean hit, boolean empty, ProductDetailVO detailVO) {
            this.hit = hit;
            this.empty = empty;
            this.detailVO = detailVO;
        }

        public static CacheLookupResult miss() {
            return new CacheLookupResult(false, false, null);
        }

        public static CacheLookupResult empty() {
            return new CacheLookupResult(true, true, null);
        }

        public static CacheLookupResult hit(ProductDetailVO detailVO) {
            return new CacheLookupResult(true, false, detailVO);
        }

        public boolean isHit() {
            return hit;
        }

        public boolean isEmpty() {
            return empty;
        }

        public ProductDetailVO getDetailVO() {
            return detailVO;
        }
    }
}
