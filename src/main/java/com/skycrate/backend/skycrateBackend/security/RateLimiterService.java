package com.skycrate.backend.skycrateBackend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final Cache<String, Integer> attemptsCache;

    private static final int MAX_ATTEMPTS = 5;

    public RateLimiterService() {
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public boolean isBlocked(String key) {
        Integer attempts = attemptsCache.getIfPresent(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String key) {
        int attempts = attemptsCache.getIfPresent(key) == null ? 0 : attemptsCache.getIfPresent(key);
        attemptsCache.put(key, attempts + 1);
    }

    public void resetAttempts(String key) {
        attemptsCache.invalidate(key);
    }
}