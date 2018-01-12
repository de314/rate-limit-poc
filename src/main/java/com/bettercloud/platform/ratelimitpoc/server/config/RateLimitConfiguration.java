package com.bettercloud.platform.ratelimitpoc.server.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Component
@ConfigurationProperties(prefix = "bc.rest.filters.rateLimit")
public class RateLimitConfiguration {

    /**
     * enable or disable the rate limiter
     */
    private boolean enabled;

    /**
     * the filter path. default: /*
     */
    private String path;

    /**
     * the requester header key
     */
    private String requesterIdHeaderKey;

    /**
     * Define the rate limit buckets and limiting constraints
     */
    private List<RateLimitConstraintConfig> constraints = Lists.newArrayList();


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Data
    public static class RateLimitConstraintConfig {
        // Required
        /**
         * The name of the bucket. Used for the response header.
         */
        @NotBlank
        private String name;
        /**
         * The limit for number of requests per time period, for this bucket.
         */
        private Long value;
        /**
         * The time period to enforce `value` for this bucket
         */
        private TimeUnit timeUnit;

        // Optional

        /**
         * (Optional) The regex pattern for matching requester id's. default: `constraint.name`
         */
        private String rawIdPattern;
        /**
         * (Optional) The group response header. default: `constraint.name /constraint.rawIdPattern/`
         */
        private String rawGroupHeaderValue;
    }
}
