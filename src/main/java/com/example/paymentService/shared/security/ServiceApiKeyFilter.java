package com.example.paymentService.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ==========================================
 * SERVICE API KEY FILTER
 * ==========================================
 * 
 * Detta filter körs FÖRE GatewayHeaderAuthenticationFilter och hanterar
 * autentisering för service-to-service kommunikation.
 * 
 * FLOW:
 * 1. Kolla om request har "X-Internal-API-Key" header
 * 2. Om JA och API key är KORREKT:
 * → Skapa en "INTERNAL_SERVICE" authentication
 * → Sätt i SecurityContext
 * → Request går igenom utan JWT
 * 3. Om NEJ eller FELAKTIG API key:
 * → Gör ingenting, låt GatewayHeaderAuthenticationFilter hantera
 * 
 * ANVÄNDNING:
 * - AdminService anropar /api/admin/payments med X-Internal-API-Key header
 * 
 * PRODUKTION:
 * - API Key lagras i environment variable (SERVICE_API_KEY)
 * - Alla services delar SAMMA key
 * - I Kubernetes: lagras som Secret
 * - Roteras regelbundet
 */
@Component
public class ServiceApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ServiceApiKeyFilter.class);

    /**
     * Namnet på HTTP-headern som innehåller API key
     */
    private static final String API_KEY_HEADER = "X-Internal-API-Key";

    /**
     * API key som andra services måste skicka
     * Laddas från application.properties (service.api.key)
     * 
     * I produktion: Lägg i environment variable eller secret manager
     */
    @Value("${service.api.key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // STEG 1: Hämta API key från request header
        String apiKey = request.getHeader(API_KEY_HEADER);

        // STEG 2: Om API key finns i headern
        if (apiKey != null && !apiKey.isEmpty()) {

            log.debug("🔑 API Key detected in request to: {}", request.getRequestURI());

            // STEG 3: Validera API key
            if (apiKey.equals(validApiKey)) {

                log.debug("Valid API key - service-to-service auth for: {}", request.getRequestURI());

                // STEG 4: Skapa CustomUserAuthentication för service-to-service
                // Använder special constructor för services (no userId)
                CustomUserAuthentication authentication = new CustomUserAuthentication(
                        "INTERNAL_SERVICE", // Service name (no user ID)
                        "INTERNAL_SERVICE" // Role
                );

                // STEG 5: Sätt authentication i SecurityContext
                // Nu vet Spring Security att denna request är autentiserad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("   SecurityContext updated with INTERNAL_SERVICE CustomUserAuthentication");

            } else {
                // Felaktig API key
                log.warn("⚠️ INVALID API Key detected!");
                log.warn("   Request URI: {}", request.getRequestURI());
                log.warn("   Remote IP: {}", request.getRemoteAddr());

                // Vi returnerar 401 Unauthorized direkt
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Unauthorized\", \"message\": \"Invalid API Key\"}");
                return; // Stoppa request här
            }
        } else {
            // Ingen API key i header - detta är OK
            // Det betyder att det är en vanlig request med JWT token
            // Vi låter GatewayHeaderAuthenticationFilter hantera den
            log.debug("No API Key in request - will check for Gateway headers");
        }

        // STEG 6: Fortsätt filter-kedjan
        // Om vi kom hit betyder det antingen:
        // A) Valid API key (authentication är satt)
        // B) Ingen API key (låt Gateway header filter hantera)
        filterChain.doFilter(request, response);
    }

    /**
     * Loggar filtrets konfiguration vid start
     */
    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        log.debug("ServiceApiKeyFilter initialized: header={} keyConfigured={}",
                API_KEY_HEADER, validApiKey != null ? "YES" : "NO");
    }
}
