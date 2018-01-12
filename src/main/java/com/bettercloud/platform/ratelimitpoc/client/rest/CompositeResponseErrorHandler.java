package com.bettercloud.platform.ratelimitpoc.client.rest;

import com.bettercloud.platform.ratelimitpoc.client.models.CompositeException;
import com.google.common.collect.Lists;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;

public class CompositeResponseErrorHandler implements ResponseErrorHandler {

    private final List<ResponseErrorHandler> delegateHandlers;

    public CompositeResponseErrorHandler(List<ResponseErrorHandler> delegateHandlers) {
        this.delegateHandlers = delegateHandlers;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        for (ResponseErrorHandler delegateHandler : delegateHandlers) {
            if (delegateHandler.hasError(response)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        List<Throwable> exceptions = Lists.newArrayList();
        for (ResponseErrorHandler delegateHandler : delegateHandlers) {
            try {
                if (delegateHandler.hasError(response)) {
                    delegateHandler.handleError(response);
                }
            } catch (Throwable e) {
                exceptions.add(e);
            }
        }
        if (exceptions.size() == 1) {
            Throwable e = exceptions.get(0);
            throw new RestClientException(e.getMessage(), e);
        } else if (exceptions.size() > 1) {
            throw new CompositeException("Multiple exception throw for request", exceptions);
        }
    }
}
