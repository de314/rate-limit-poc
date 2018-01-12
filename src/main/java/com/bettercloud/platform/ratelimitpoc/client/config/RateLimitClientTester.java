package com.bettercloud.platform.ratelimitpoc.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RateLimitClientTester implements CommandLineRunner {

    // You can provide a Rest Template bean that will get decorated or you can just autowire
    // the provided one.
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) throws Exception {
        for (int i=0;i<100;i++) {
            // No need to do anything special. Just use it as normal and it will apply the correct Rate Limit headers.
            // See com.bettercloud.platform.ratelimitpoc.client.rest.RateLimitRequestInterceptor for more details
            ResponseEntity<String> res = restTemplate.getForEntity("http://localhost:9090/rl/fib/calc/1", String.class);
            HttpStatus statusCode = res.getStatusCode();
            System.out.println();
            log.info("statusCode = " + statusCode);
            res.getHeaders().entrySet().stream()
                    .filter(e -> e.getKey().startsWith("X-Rate-Limit"))
                    .forEach(e -> log.info("{}: {}", e.getKey(), e.getValue()));
        }
    }
}
