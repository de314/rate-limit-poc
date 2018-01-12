package com.bettercloud.platform.ratelimitpoc.client.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        log.warn("Should implement exponential back off to try again.");
    }
}
