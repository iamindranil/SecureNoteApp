package com.secure.notes.services.impl;

import com.secure.notes.security.jwt.JwtUtils;
import com.secure.notes.services.JwtBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class JwtBlacklistServiceImpl implements JwtBlacklistService {
    @Autowired
    JwtUtils jwtUtils;

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "jwt:blacklist:";

    public JwtBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token) {
        Date expirationdate=jwtUtils.getExpirationDateFromJwtToken(token);
        long remainingTimeInMillis=expirationdate.getTime()-System.currentTimeMillis();
        if(remainingTimeInMillis>0){
            redisTemplate.opsForValue().set(
                    KEY_PREFIX+token,
                    "blacklisted",
                    Duration.ofMillis(remainingTimeInMillis));
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX+token));
    }
}
