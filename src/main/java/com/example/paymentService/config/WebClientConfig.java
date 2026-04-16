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

    @Value("${USER_SERVICE_URL:http://user-service:8081}")
    private String userServiceUrl;

    /**
     * WebClient för UserService
     * 
     * Skapar WebClient UTAN @LoadBalanced för att fungera i Railway.
     * Railway blockerar IP-till-IP kommunikation, så vi måste använda direkt URL.
     * 
     * Base URL från miljövariabel:
     * - Railway: USER_SERVICE_URL=http://userservice:8081 (Railway DNS)
     * - Lokal: USER_SERVICE_URL=http://localhost:8081 (eller Eureka)
     * 
     * Endpoints som anropas:
     * - POST /api/subscriptions → Skapa subscription efter lyckad betalning
     * 
     * SÄKERHET:
     * - X-Internal-API-Key: Service-to-service authentication
     * - X-Internal-Source: Identifierar payment-service som källa
     */
    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-Internal-API-Key", serviceApiKey)
                .defaultHeader("X-Internal-Source", "payment-service")
                .build();
    }
}
