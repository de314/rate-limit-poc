package com.bettercloud.platform.ratelimitpoc.server.rest.filters;

import com.bettercloud.platform.ratelimitpoc.server.config.RateLimitConstraint;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DefaultRateLimitFilter extends OncePerRequestFilter {

    public static final RateLimitConstraint DEFAULT_GLOBAL_CONSTRAINT = RateLimitConstraint.builder()
            .name("default-global")
            .value(-1L)
            .timeUnit(TimeUnit.DAYS)
            .rawIdPattern(".*")
            .idPattern(Pattern.compile(".*"))
            .build();
    public static final int TOO_MANY_REQUESTS = 429;
    public static final byte[] ERROR_BODY = "{\"errors\":[{\"code\":429,\"message\":\"Rate limit exceeded\"}]}".getBytes();
    public static final RateLimitKey NO_OP_RATE_LIMIT_MASKED_TIME = RateLimitKey.builder()
            .resetTime(0)
            .build();
    public static final long NO_OP_BUCKET_COUNT = Integer.MIN_VALUE + 1;

    private final Cache<String, RateLimitConstraint> recentConstraintsCache;
    private final List<RateLimitConstraint> constraints;
    private final RateLimitCache rateLimitCache;
    private final RateLimitConstraint defaultConstraint;
    private final String requesterIdHeader;

    public DefaultRateLimitFilter(List<RateLimitConstraint> constraints, RateLimitCache rateLimitCache,
            @Value("${bc.rest.filters.rate-limit.requesterIdHeaderKey:X-Rate-Limit-ID}")
            String requesterIdHeader) {
        this.requesterIdHeader = requesterIdHeader;
        this.recentConstraintsCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .build();
        this.constraints = constraints.isEmpty() ? Lists.newArrayList(DEFAULT_GLOBAL_CONSTRAINT) : constraints;
        this.constraints.forEach( c -> {
            if (c.getRawIdPattern() == null) {
                c.setRawIdPattern(c.getName());
            }
            c.setIdPattern(Pattern.compile(c.getRawIdPattern()));
            if (c.getRawGroupHeader() == null) {
                c.setRawGroupHeader(String.format("%s (%s)", c.getName(), c.getRawIdPattern()));
            }
        });
        this.defaultConstraint = constraints.stream()
                .filter(c -> c.getRawIdPattern().equals(".*"))
                .findFirst()
                .orElse(DEFAULT_GLOBAL_CONSTRAINT);

        this.rateLimitCache = rateLimitCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String configKey = Optional.ofNullable(request.getHeader(requesterIdHeader))
                .orElse("global");

        RateLimitConstraint constraint = Optional.ofNullable(recentConstraintsCache.getIfPresent(configKey))
                .orElse(defaultConstraint);
        for (RateLimitConstraint c : constraints) {
            Matcher matcher = c.getIdPattern().matcher(configKey);
            if (matcher.find()) {
                constraint = c;
                break;
            }
        }

        Long bucketCount = NO_OP_BUCKET_COUNT;
        RateLimitKey rateLimitKey = NO_OP_RATE_LIMIT_MASKED_TIME;
        if (constraint.getValue() > 0) {
            rateLimitKey = rateLimitCache.getKey(constraint);
            bucketCount = rateLimitCache.getAndIncrement(rateLimitKey);
        }

        response.addHeader("X-Rate-Limit-ID", configKey);
        response.addHeader("X-Rate-Limit-Group", constraint.getRawGroupHeader());
        response.addHeader("X-Rate-Limit-Limit", constraint.getValue().toString());
        response.addHeader("X-Rate-Limit-Remaining", constraint.getValue() - bucketCount + "");
        response.addHeader("X-Rate-Limit-Reset", rateLimitKey.getResetTime() + "");

        if (bucketCount <= constraint.getValue()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(TOO_MANY_REQUESTS);
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.getOutputStream().write(ERROR_BODY);
        }
    }

    @Override
    public void destroy() {

    }
}
