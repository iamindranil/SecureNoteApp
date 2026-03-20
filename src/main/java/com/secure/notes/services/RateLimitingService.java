package com.secure.notes.services;

public interface RateLimitingService {
    boolean isRateLimited(String username);
    void clearRateLimit(String username);
}
