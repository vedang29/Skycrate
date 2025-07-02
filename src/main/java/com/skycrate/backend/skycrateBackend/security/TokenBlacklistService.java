package com.skycrate.backend.skycrateBackend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklistCache;

    public TokenBlacklistService() {
        this.blacklistCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    public void blacklistToken(String token) {
        blacklistCache.put(token, true);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistCache.getIfPresent(token) != null;
    }
}