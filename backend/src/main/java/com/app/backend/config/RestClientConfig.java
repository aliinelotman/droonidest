package com.app.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Provides pre-configured RestClient beans for external HTTP calls.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient googleRestClient() {
        return RestClient.builder()
                .requestFactory(googleRequestFactory())
                .build();
    }

    private SimpleClientHttpRequestFactory googleRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
