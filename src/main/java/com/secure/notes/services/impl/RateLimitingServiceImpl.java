package com.secure.notes.services.impl;

import com.secure.notes.services.RateLimitingService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingServiceImpl implements RateLimitingService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "rate_limit:2fa:";

    public RateLimitingServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isRateLimited(String username) {
        String key=KEY_PREFIX+username;
        //atomically increment attempt count in redis
        Long attempts = redisTemplate.opsForValue().increment(key);
        if(attempts!=null && attempts==1){
            redisTemplate.expire(key,BLOCK_DURATION);
        }
        return attempts!=null && attempts>MAX_ATTEMPTS;
    }

    @Override
    public void clearRateLimit(String username) {
        redisTemplate.delete(KEY_PREFIX+username);
    }
}
