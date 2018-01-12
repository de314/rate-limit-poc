package com.bettercloud.platform.ratelimitpoc.server.rest.filters;

import com.bettercloud.platform.ratelimitpoc.server.config.RateLimitConstraint;

public interface RateLimitCache {

    /**
     * @return The rate limit key info.
     */
    RateLimitKey getKey(RateLimitConstraint constraint);

    /**
     *
     * @param key The rate limit bucket key. Use {@code RateLimitCache#getKey(RateLimitConstraint)}
     * @return The value before incrementing. Will return 0 on initial request of key.
     */
    Long getAndIncrement(RateLimitKey key);

    /**
     *
     * @param key The rate limit bucket key. Use {@code RateLimitCache#getKey(RateLimitConstraint)}
     * @return The value before incrementing. Will return 1 on initial request of key.
     */
    Long incrementAndGet(RateLimitKey key);
}
