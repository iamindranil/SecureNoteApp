package com.secure.notes.services;

public interface JwtBlacklistService {
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String token);
}
