// NEED TO IMPLEMENT SAHI SE
package com.skycrate.backend.skycrateBackend.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        return attempts.getOrDefault(ip, 0) >= 5;
    }

    public void recordFailedAttempt(String ip) {
        attempts.put(ip, attempts.getOrDefault(ip, 0) + 1);
    }

    public void resetAttempts(String ip) {
        attempts.remove(ip);
    }
}