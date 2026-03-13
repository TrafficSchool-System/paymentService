package com.example.paymentService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================
 * WEBCLIENT CONFIGURATION
 * ==========================================
 * Konfigurerar load-balanced WebClient för service-to-service kommunikation.
 * 
 * ARKITEKTUR:
 * - PaymentService använder UserService för att skapa subscriptions
 * - @LoadBalanced aktiverar Eureka service discovery
 * - Service namn (http://user-service) översätts automatiskt till IP:PORT
 * 
 * SÄKERHET:
 * - API key används för service-to-service autentisering
 * - X-Internal-Source header identifierar anropande service
 * 
 * ANVÄNDNING:
 * - PaymentService -> UserService för att aktivera subscriptions efter
 * betalning
 * - Inga direkta HTTP-anrop till localhost:PORT
 * - Eureka hanterar automatisk service discovery och load balancing
 */
@Configuration
public class WebClientConfig {

    @Value("${service.api.key}")
    private String serviceApiKey;

    /**
     * WebClient.Builder med load balancing
     * 
     * @LoadBalanced aktiverar service discovery via Eureka
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json");
    }

    /**
     * WebClient för UserService
     * Base URL: http://user-service (resolveras via Eureka)
     * 
     * Endpoints som anropas:
     * - POST /api/subscriptions → Skapa subscription efter lyckad betalning
     * 
     * SÄKERHET:
     * - X-Internal-API-Key: Service-to-service authentication
     * - X-Internal-Source: Identifierar payment-service som källa
     */
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://user-service")
                .defaultHeader("X-Internal-API-Key", serviceApiKey)
                .defaultHeader("X-Internal-Source", "payment-service")
                .build();
    }
}
