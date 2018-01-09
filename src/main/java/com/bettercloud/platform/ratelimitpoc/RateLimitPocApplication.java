package com.bettercloud.platform.ratelimitpoc;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
@RequestMapping("/fib")
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