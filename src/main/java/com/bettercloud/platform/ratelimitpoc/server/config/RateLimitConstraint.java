package com.bettercloud.platform.ratelimitpoc.server.config;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Data
@Builder
public class RateLimitConstraint {
    // Required
    @NonNull private String name;
    @NonNull private Long value;
    @NonNull private TimeUnit timeUnit;

    // Optional
    @NonNull private String rawIdPattern;

    // Derived
    private Pattern idPattern;
    private String rawGroupHeader;
}
