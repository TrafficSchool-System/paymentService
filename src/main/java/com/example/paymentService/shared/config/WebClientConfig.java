package com.example.paymentService.shared.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

/**
 * ==========================================
 * WEBCLIENT CONFIGURATION
 * ==========================================
 * Konfigurerar WebClient för service-to-service kommunikation.
 *
 * Använder Azure Container Apps intern DNS direkt (http://user-service).
 * Ingen @LoadBalanced/Eureka behövs — Azure hanterar lastbalansering.
 * 8s response timeout förhindrar hängande anrop.
 */
@Configuration
public class WebClientConfig {

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Value("${user-service.base-url:http://user-service}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(8));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(userServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-Internal-API-Key", serviceApiKey)
                .defaultHeader("X-Internal-Source", "payment-service")
                .build();
    }
}
