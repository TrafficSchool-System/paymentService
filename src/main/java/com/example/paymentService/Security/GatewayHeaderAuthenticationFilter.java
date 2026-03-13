package com.example.paymentService.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * ==========================================
 * GATEWAY HEADER AUTHENTICATION FILTER
 * ==========================================
 * 
 * ARKITEKTUR:
 * Client → Gateway (validerar JWT) → PaymentService (läser headers)
 * 
 * VARFÖR DETTA FILTER EXISTERAR:
 * - I en microservice-arkitektur ska ENDAST Gateway validera JWT
 * - PaymentService litar 100% på headers från Gateway
 * - Gateway sätter dessa headers efter lyckad JWT-validering
 * 
 * FLOW:
 * 1. Gateway validerar JWT token
 * 2. Gateway extraherar userId, email, role från JWT
 * 3. Gateway sätter headers: X-User-Id, X-User-Email, X-User-Role
 * 4. Detta filter läser headers och skapar Spring Security Authentication
 * 
 * SÄKERHET:
 * - Dessa headers får ALDRIG nå PaymentService direkt från externa klienter
 * - Gateway måste ALLTID strippa bort dessa headers från inkommande requests
 * - Endast Gateway får sätta dessa headers
 * 
 * HEADERS:
 * - X-User-Id: Long (userId från JWT)
 * - X-User-Email: String (email från JWT)
 * - X-User-Role: String (USER eller ADMIN)
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayHeaderAuthenticationFilter.class);

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && userEmail != null && userRole != null) {

            log.info("🌐 Gateway headers detected - User authenticated by Gateway");
            log.debug("   User ID: {}, Email: {}, Role: {}", userId, userEmail, userRole);

            try {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        Collections.singletonList(authority));

                request.setAttribute("userId", Long.parseLong(userId));
                request.setAttribute("userEmail", userEmail);
                request.setAttribute("userRole", userRole);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("✅ Authentication set from Gateway headers - {} with role {}", userEmail, userRole);

            } catch (Exception e) {
                log.error("❌ Failed to create authentication from Gateway headers: {}", e.getMessage());
            }
        } else {
            log.debug("⏭️ No Gateway headers found - Request may be public");
        }

        filterChain.doFilter(request, response);
    }
}
