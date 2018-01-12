package com.bettercloud.platform.ratelimitpoc;

import com.bettercloud.platform.ratelimitpoc.client.models.CompositeException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootApplication
public class RateLimitPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateLimitPocApplication.class, args);
	}

    @Bean
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
    public CommandLineRunner filterPrinter(RestTemplate restTemplate) {
        return args -> {
            for (int i=0;i<100;i++) {
                ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:9090/rl/fib/calc/1", String.class);
                HttpStatus statusCode = res.getStatusCode();
                System.out.println();
                log.info("statusCode = " + statusCode);
                res.getHeaders().entrySet().stream()
                        .filter(e -> e.getKey().startsWith("X-Rate-Limit"))
                        .forEach(e -> log.info("{}: {}", e.getKey(), e.getValue()));
            }
        };
    }
}



