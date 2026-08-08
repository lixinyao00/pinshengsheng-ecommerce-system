package com.pinshengsheng.auth.service.impl;

import com.pinshengsheng.auth.service.SignService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;

@Service
public class SignServiceImpl implements SignService {

    private static final String SIGN_KEY_PREFIX = "user:sign:";
    private static final ZoneId PROJECT_ZONE = ZoneId.of("Asia/Shanghai");

    private final StringRedisTemplate stringRedisTemplate;

    public SignServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean sign(Long userId, Integer day) {
        validate(userId, day);

        String key = buildSignKey(userId);

        // day 从1开始，Bitmap 的偏移量从0开始，所以这里要减1
        Boolean previous = stringRedisTemplate.opsForValue()
                .setBit(key, day - 1, true);

        // 保存一年多一点，避免历史签到数据长期占用 Redis
        stringRedisTemplate.expire(key, Duration.ofDays(400));

        // 如果之前已经是1，说明用户今天重复签到
        return !Boolean.TRUE.equals(previous);
    }

    @Override
    public boolean signed(Long userId, Integer day) {
        validate(userId, day);

        Boolean signed = stringRedisTemplate.opsForValue()
                .getBit(buildSignKey(userId), day - 1);

        return Boolean.TRUE.equals(signed);
    }

    @Override
    public Long signCount(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户编号必须大于0");
        }

        String key = buildSignKey(userId);

        // BITCOUNT 属于 Redis 的字符串命令，通过底层连接统计所有为1的位
        Long count = stringRedisTemplate.execute(
                (RedisCallback<Long>) connection ->
                        connection.bitCount(key.getBytes(StandardCharsets.UTF_8))
        );

        return count == null ? 0L : count;
    }

    private String buildSignKey(Long userId) {
        YearMonth currentMonth = YearMonth.now(PROJECT_ZONE);
        return SIGN_KEY_PREFIX + currentMonth + ":" + userId;
    }

    private void validate(Long userId, Integer day) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户编号必须大于0");
        }

        if (day == null || day < 1 || day > 31) {
            throw new IllegalArgumentException("签到日期必须在1到31之间");
        }
    }
}
