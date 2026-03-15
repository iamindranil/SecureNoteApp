package com.secure.notes.services.impl;

import com.secure.notes.services.JwtBlacklistService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "jwt:blacklist:";

    public JwtBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token) {
        redisTemplate.opsForValue().set(KEY_PREFIX+token,"blacklisted", Duration.ofHours(1));
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX+token));
    }
}
