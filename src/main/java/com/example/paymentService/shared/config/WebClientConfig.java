package com.example.paymentService.shared.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================
 * WEBCLIENT CONFIGURATION
 * ==========================================
 * Environment-aware WebClient configuration för service-to-service
 * kommunikation.
 * 
 * ARKITEKTUR:
 * - PaymentService använder UserService för att skapa subscriptions
 * 
 * ENVIRONMENTS:
 * - LOCAL (default): Direct HTTP utan LoadBalancer (localhost:8081)
 * - PRODUCTION: LoadBalanced med Eureka service discovery (http://user-service)
 * 
 * VARFÖR TVÅ OLIKA KONFIGURATIONER?
 * - @LoadBalanced försöker resolva ALLA URLs via Eureka (även localhost)
 * - I lokal miljö finns services inte i Eureka på localhost
 * - I produktion (Docker/K8s) finns services registrerade i Eureka med
 * service-namn
 * 
 * SÄKERHET:
 * - X-Internal-API-Key: Service-to-service authentication
 * - X-Internal-Source: Identifierar anropande service
 */
@Configuration
public class WebClientConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Value("${user-service.base-url:http://user-service}")
    private String userServiceUrl;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /**
     * LoadBalanced WebClient.Builder for PRODUCTION
     * 
     * Används när spring.profiles.active=prod
     * Eureka resolvar service-namn (http://user-service) till faktisk IP:PORT
     */
    @Bean
    @Qualifier("loadBalanced")
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        log.info("☁️ Creating LoadBalanced WebClient.Builder for production service discovery");
        return WebClient.builder();
    }

    /**
     * Standard WebClient.Builder for LOCAL development
     * 
     * Används när spring.profiles.active=local
     * Gör direkta HTTP-anrop utan Eureka (localhost:8081)
     */
    @Bean
    @Qualifier("standard")
    public WebClient.Builder standardWebClientBuilder() {
        log.info("🏠 Creating standard WebClient.Builder for local development");
        return WebClient.builder();
    }

    /**
     * WebClient för UserService med SMART ROUTING
     * 
     * - LOCAL: Använder standardWebClientBuilder → Direct HTTP
     * - PRODUCTION: Använder loadBalancedWebClientBuilder → Eureka
     * 
     * Endpoints som anropas:
     * - POST /api/subscriptions → Skapa subscription efter lyckad betalning
     * 
     * SÄKERHET:
     * - X-Internal-API-Key: Service-to-service authentication
     * - X-Internal-Source: Identifierar payment-service som källa
     */
    @Bean
    public WebClient userServiceWebClient(
            @Qualifier("standard") WebClient.Builder standardWebClientBuilder,
            @Qualifier("loadBalanced") WebClient.Builder loadBalancedWebClientBuilder) {

        boolean isLocal = "local".equals(activeProfile) || "default".equals(activeProfile);
        WebClient.Builder builder = isLocal ? standardWebClientBuilder : loadBalancedWebClientBuilder;

        log.info("🌐 Creating UserService WebClient [profile={}, url={}, loadBalanced={}]",
                activeProfile, userServiceUrl, !isLocal);

        return builder
                .baseUrl(userServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("X-Internal-API-Key", serviceApiKey)
                .defaultHeader("X-Internal-Source", "payment-service")
                .build();
    }
}
