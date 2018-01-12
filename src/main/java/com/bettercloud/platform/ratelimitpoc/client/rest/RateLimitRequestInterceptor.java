package com.bettercloud.platform.ratelimitpoc.client.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitRequestInterceptor implements ClientHttpRequestInterceptor {

    private final String requesterId;

    public RateLimitRequestInterceptor(@Value("${spring.application.name:global}") String requesterId) {
        this.requesterId = requesterId;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("X-BC-Rate-Limit-ID", requesterId);
        return execution.execute(request, body);
    }
}
