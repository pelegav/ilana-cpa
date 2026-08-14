package com.ilanacpa.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * In-memory per-key rate limiting. Works correctly for a single backend instance only —
 * if the app is later scaled horizontally, these buckets won't be shared across instances
 * and this should move to a Redis-backed implementation (bucket4j-redis).
 */
@Service
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> loginByIp = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginByAccount = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshByIp = new ConcurrentHashMap<>();

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean tryConsumeLoginByIp(String ip) {
        return bucketFor(loginByIp, ip, properties.loginPerIpPerMinute(), Duration.ofMinutes(1)).tryConsume(1);
    }

    public boolean tryConsumeLoginByAccount(String email) {
        return bucketFor(loginByAccount, email, properties.loginPerAccountPer15Min(), Duration.ofMinutes(15)).tryConsume(1);
    }

    public boolean tryConsumeRefreshByIp(String ip) {
        return bucketFor(refreshByIp, ip, properties.refreshPerIpPerMinute(), Duration.ofMinutes(1)).tryConsume(1);
    }

    private Bucket bucketFor(Map<String, Bucket> cache, String key, int capacity, Duration period) {
        return cache.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, period).build())
                .build());
    }
}
