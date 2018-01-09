package com.bettercloud.platform.ratelimitpoc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootApplication
public class RateLimitPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateLimitPocApplication.class, args);
	}

    @Bean
//    @Profile("local")
    public Filter corsFilter() {
        return new Filter() {

            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
                HttpServletResponse response = (HttpServletResponse) res;
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
                response.setHeader("Access-Control-Expose-Headers", "Location");
                chain.doFilter(req, res);
            }

            public void init(FilterConfig filterConfig) {}

            public void destroy() {}

        };
    }

    @Bean
    public FilterRegistrationBean rateLimitFilterRegistration(LocalRateLimitFilter rateLimitFilter) {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(rateLimitFilter);
        reg.addUrlPatterns("/rl/*");
//        reg.addInitParameter("paramName", "paramValue");
        reg.setName("rateLimit");
        reg.setOrder(Integer.MAX_VALUE);
        return reg;
    }

    @Bean
    public List<RateLimitConstraint> rateLimitConstraints() {
	    return Lists.newArrayList(RateLimitConstraint.builder()
                .name("global")
                .value(2L)
                .timeUnit(TimeUnit.SECONDS)
                .build());
    }

    @Bean
    public CommandLineRunner filterPrinter(List<Filter> filters) {
	    return args -> filters.forEach(f -> System.out.println(String.format("%s", f.getClass().getCanonicalName())));
    }

}

interface FibonacciService {

	long fib(int i);
}

@Service
class SlowFibonacciService implements FibonacciService {

    @Override
    public long fib(int i) {
        if (i <= 1) {
            return 1;
        }
        return fib(i - 1) + fib(i - 2);
    }
}

@RestController
@RequestMapping({"/fib", "/rl/fib"})
class FibonacciController {

	private final FibonacciService fibonacciService;

    FibonacciController(FibonacciService fibonacciService) {
        this.fibonacciService = fibonacciService;
    }


    @GetMapping("/calc/{i}")
	public FibResult calc(@PathVariable("i") int i) {
        long startTime = System.currentTimeMillis();
        long result = fibonacciService.fib(i);
        long duration = System.currentTimeMillis() - startTime;
        return FibResult.builder()
                .result(result)
                .startTime(startTime)
                .duration(duration)
                .build();
	}
}

@Data
@Builder
class FibResult {
    private long result;
    private long startTime;
    private long duration;
}

@Data
@Builder
class RateLimitConstraint {
    private String name;
    private Long value;
    private TimeUnit timeUnit;
}

@Data
@Builder
class RateLimitMaskedTime {
    private long curr;
    private long masked;
    private long reset;
}

@Service
class LocalRateLimitFilter extends OncePerRequestFilter {

    public static final RateLimitConstraint DEFAULT_GLOBAL_CONSTRAINT = RateLimitConstraint.builder()
            .name("global")
            .value(10L)
            .timeUnit(TimeUnit.MINUTES)
            .build();
    public static final int TOO_MANY_REQUESTS = 429;
    public static final byte[] ERROR_BODY = "{\"errors\":[{\"code\":429,\"message\":\"Rate limit exceeded\"}]}".getBytes();
    public static final long SECOND_MASK = 1000;
    public static final long MINUTE_MASK = SECOND_MASK * 60;
    public static final long HOUR_MASK = MINUTE_MASK * 60;
    public static final long DAY_MASK = HOUR_MASK * 24;

    private final Map<String, RateLimitConstraint> constraintsMap;
    private final Map<TimeUnit, Cache<String, Long>> cacheMap;
    private final RateLimitConstraint defaultConstraint;

    public LocalRateLimitFilter(List<RateLimitConstraint> constraints) {

        constraints = constraints.isEmpty() ? Lists.newArrayList(DEFAULT_GLOBAL_CONSTRAINT) : constraints;
        constraintsMap = Collections.unmodifiableMap(
                constraints.stream().collect(Collectors.toMap(
                        item -> item.getName(),
                        item -> item
                ))
        );
        this.defaultConstraint = constraints.stream()
                .filter(c -> c.getName().equals("global"))
                .findFirst()
                .orElse(DEFAULT_GLOBAL_CONSTRAINT);

        cacheMap = new HashMap<TimeUnit, Cache<String, Long>>() {{
            this.put(TimeUnit.SECONDS, CacheBuilder.newBuilder()
                    .expireAfterWrite(3, TimeUnit.SECONDS)
                    .build());
            this.put(TimeUnit.MINUTES, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    .build());
            this.put(TimeUnit.HOURS, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.HOURS)
                    .build());
            this.put(TimeUnit.DAYS, CacheBuilder.newBuilder()
                    .expireAfterWrite(2, TimeUnit.DAYS)
                    .build());
        }};
    }

    public RateLimitMaskedTime getMaskedTime(RateLimitConstraint constraint) {
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
        return RateLimitMaskedTime.builder()
                .curr(currTime)
                .masked(masked)
                .reset(reset)
                .build();
    }

    public String getCacheKey(RateLimitMaskedTime time, RateLimitConstraint constraint) {
        return String.format("%s-%d", constraint.getName(), time.getMasked());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String configKey = "global";
        // TODO: get key from request

        RateLimitConstraint constraint = constraintsMap.getOrDefault(configKey, defaultConstraint);
        RateLimitMaskedTime rateLimitTime = getMaskedTime(constraint);

        String cacheKey = getCacheKey(rateLimitTime, constraint);
        Cache<String, Long> cache = cacheMap.get(constraint.getTimeUnit());

        Long bucketCount;
        synchronized (cache) {
            bucketCount = cache.getIfPresent(cacheKey);
            if (bucketCount == null) {
                bucketCount = 0L;
            }
            cache.put(cacheKey, ++bucketCount);
        }

        response.addHeader("x-rate-limit-group", constraint.getName());
        response.addHeader("x-rate-limit-limit", constraint.getValue().toString());
        response.addHeader("x-rate-limit-remaining", constraint.getValue() - bucketCount + "");
        response.addHeader("x-rate-limit-reset", rateLimitTime.getReset() + "");

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