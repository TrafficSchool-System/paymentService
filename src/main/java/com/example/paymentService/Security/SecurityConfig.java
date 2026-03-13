package com.example.paymentService.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * REFACTORED SECURITY ARCHITECTURE:
     * - Gateway validerar JWT (JwtAuthenticationGlobalFilter)
     * - Gateway sätter headers: X-User-Id, X-User-Email, X-User-Role
     * - PaymentService läser headers (GatewayHeaderAuthenticationFilter)
     * - AdminService använder API key (ServiceApiKeyFilter)
     */
    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;
    private final ServiceApiKeyFilter serviceApiKeyFilter;

    public SecurityConfig(
            GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter,
            ServiceApiKeyFilter serviceApiKeyFilter) {
        this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
        this.serviceApiKeyFilter = serviceApiKeyFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ==============================================
                        // PUBLIC ENDPOINTS - No authentication required
                        // ==============================================
                        // Swish webhook (external callback from Swish payment provider)
                        .requestMatchers("/api/webhooks/swish").permitAll()

                        // ==============================================
                        // ADMIN ENDPOINTS - Require ADMIN or INTERNAL_SERVICE role
                        // ==============================================
                        // Admin endpoints kan anropas av:
                        // 1. Admins via Gateway (med ROLE_ADMIN)
                        // 2. AdminService via API key (med ROLE_INTERNAL_SERVICE)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "INTERNAL_SERVICE")

                        // ==============================================
                        // USER ENDPOINTS - Require authentication
                        // ==============================================
                        // Packages and payments (authenticated users only)
                        .requestMatchers("/api/packages/**", "/api/payments/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(serviceApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}