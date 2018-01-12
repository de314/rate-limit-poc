package com.bettercloud.platform.ratelimitpoc.client.config;

import com.bettercloud.platform.ratelimitpoc.client.rest.CompositeResponseErrorHandler;
import com.google.common.collect.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestConfig {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(List<ClientHttpRequestInterceptor> interceptors, ResponseErrorHandler responseErrorHandler) {
        return new RestTemplate();
    }

    @Bean
    public List<RestTemplate> restTemplateDecorator(List<RestTemplate> restTemplates, List<ClientHttpRequestInterceptor> interceptors, ResponseErrorHandler responseErrorHandler) {
        for (RestTemplate restTemplate : restTemplates) {
            List<ClientHttpRequestInterceptor> newInterceptors = Lists.newArrayList();
            if (restTemplate.getInterceptors() != null) {
                newInterceptors.addAll(restTemplate.getInterceptors());
            }
            restTemplate.setInterceptors(newInterceptors);
            if (restTemplate.getErrorHandler() == null || restTemplate.getErrorHandler().getClass().isAssignableFrom(DefaultResponseErrorHandler.class)) {
                restTemplate.setErrorHandler(responseErrorHandler);
            } else {
                restTemplate.setErrorHandler(new CompositeResponseErrorHandler(
                        Lists.newArrayList(responseErrorHandler, restTemplate.getErrorHandler())
                ));
            }
        }

        return restTemplates;
    }
}
