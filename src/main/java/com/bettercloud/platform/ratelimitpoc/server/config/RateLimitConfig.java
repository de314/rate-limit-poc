package com.bettercloud.platform.ratelimitpoc.server.config;

import com.bettercloud.platform.ratelimitpoc.server.rest.filters.DefaultRateLimitFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean rateLimitFilterRegistration(DefaultRateLimitFilter rateLimitFilter, RateLimitConfiguration rateLimitConfiguration) {
        if (rateLimitConfiguration.isEnabled()) {
            String path = Optional.ofNullable(rateLimitConfiguration.getPath()).orElse("/*");
            log.info("Applying BC rate limit filter at {}", path);
            FilterRegistrationBean reg = new FilterRegistrationBean();
            reg.setFilter(rateLimitFilter);
            reg.addUrlPatterns(path);
            reg.setName("rateLimit");
            reg.setOrder(Integer.MAX_VALUE);
            return reg;
        } else {
            log.warn("BC rate limit filter is disabled");
        }
        return null;
    }

    @Bean
    public List<RateLimitConstraint> rateLimitConstraints(RateLimitConfiguration rateLimitConfig) {
        if (rateLimitConfig != null) {
            if (rateLimitConfig.isEnabled()) {
                log.info("BC rate limit filter is enabled. Applying {} constraints", rateLimitConfig.getConstraints().size());
                return rateLimitConfig.getConstraints().stream()
                        .map(c -> {
                            String rawIdPattern = Optional.ofNullable(c.getRawIdPattern()).orElse(c.getName());
                            RateLimitConstraint constraint = RateLimitConstraint.builder()
                                    .name(c.getName())
                                    .value(c.getValue())
                                    .timeUnit(c.getTimeUnit())
                                    .rawIdPattern(rawIdPattern)
                                    .idPattern(Pattern.compile(rawIdPattern))
                                    .rawGroupHeader(Optional.ofNullable(c.getRawGroupHeaderValue())
                                            .orElseGet(() -> String.format("%s (/%s/)", c.getName(), c.getRawIdPattern()))
                                    )
                                    .build();
                            log.info("Applying {}", constraint);
                            return constraint;
                        })
                        .collect(Collectors.toList());
            }
        } else {
            log.warn("BC rate limit filter was not configured. Using defaults");
        }
        return Collections.emptyList();
    }
}
