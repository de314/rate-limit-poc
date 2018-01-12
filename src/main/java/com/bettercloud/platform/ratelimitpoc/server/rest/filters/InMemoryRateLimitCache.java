package com.bettercloud.platform.ratelimitpoc.server.rest.filters;

import com.bettercloud.platform.ratelimitpoc.server.config.RateLimitConstraint;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class InMemoryRateLimitCache implements RateLimitCache {

    public static final long SECOND_MASK = 1000;
    public static final long MINUTE_MASK = SECOND_MASK * 60;
    public static final long HOUR_MASK = MINUTE_MASK * 60;
    public static final long DAY_MASK = HOUR_MASK * 24;

    private final Map<TimeUnit, Cache<String, Long>> cacheMap;

    public InMemoryRateLimitCache() {
        cacheMap = new HashMap<TimeUnit, Cache<String, Long>>() {{
            this.put(TimeUnit.SECONDS, CacheBuilder.newBuilder()
                    .expireAfterWrite(3, TimeUnit.SECONDS)
                    .concurrencyLevel(1)
                    .build());
            this.put(TimeUnit.MINUTES, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .concurrencyLevel(1)
                    .build());
            this.put(TimeUnit.HOURS, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.HOURS)
                    .concurrencyLevel(1)
                    .build());
            this.put(TimeUnit.DAYS, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.DAYS)
                    .concurrencyLevel(1)
                    .build());
        }};
    }

    @Override
    public RateLimitKey getKey(RateLimitConstraint constraint) {
        long mask = SECOND_MASK;
        switch (constraint.getTimeUnit()) {
            case MINUTES:
                mask = MINUTE_MASK;
                break;
            case HOURS:
                mask = HOUR_MASK;
                break;
            case DAYS:
                mask = DAY_MASK;
                break;
        }
        long currTime = System.currentTimeMillis();
        long masked = currTime / mask;
        long reset = (masked + 1) * mask - currTime;
        String keyValue = String.format("%s-%d", constraint.getName(), masked);
        return RateLimitKey.builder()
                .keyValue(keyValue)
                .constraint(constraint)
                .currTime(currTime)
                .maskedTime(masked)
                .resetTime(reset)
                .build();
    }

    @Override
    public Long getAndIncrement(RateLimitKey key) {
        return incrementAndGet(key) - 1;
    }

    @Override
    public Long incrementAndGet(RateLimitKey key) {
        Cache<String, Long> cache = cacheMap.get(key.getConstraint().getTimeUnit());
        Long bucketCount;

        synchronized (cache) {
            bucketCount = cache.getIfPresent(key.getKeyValue());
            if (bucketCount == null) {
                bucketCount = 0L;
            }
            cache.put(key.getKeyValue(), ++bucketCount);
        }

        return bucketCount;
    }
}
