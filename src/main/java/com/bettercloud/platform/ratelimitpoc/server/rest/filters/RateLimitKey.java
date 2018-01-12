package com.bettercloud.platform.ratelimitpoc.server.rest.filters;

import com.bettercloud.platform.ratelimitpoc.server.config.RateLimitConstraint;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitKey {
    private String keyValue;
    private RateLimitConstraint constraint;
    private long currTime;
    private long maskedTime;
    private long resetTime;
}
