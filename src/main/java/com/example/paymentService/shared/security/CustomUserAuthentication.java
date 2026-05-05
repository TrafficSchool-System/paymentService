package com.example.paymentService.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * ==========================================
 * CUSTOM USER AUTHENTICATION
 * ==========================================
 * 
 * Spring Security Authentication object for Gateway-first architecture.
 * 
 * SINGLE SOURCE OF TRUTH for user identity in PaymentService.
 * 
 * USAGE IN CONTROLLERS:
 * ```java
 * @GetMapping("/payments")
 * public ResponseEntity<?> getPayments(
 * 
 * @AuthenticationPrincipal CustomUserAuthentication auth) {
 *                          Long userId = auth.getUserId(); // Type-safe
 *                          String email = auth.getEmail(); // Type-safe
 *                          boolean isAdmin = auth.hasRole("ADMIN"); //
 *                          Type-safe
 *                          // ...
 *                          }
 *                          ```
 * 
 *                          CREATED BY:
 *                          - GatewayHeaderAuthenticationFilter (from Gateway
 *                          headers)
 *                          - ServiceApiKeyFilter (from API key for
 *                          service-to-service)
 * 
 *                          STORED IN:
 *                          - SecurityContext (Spring Security standard)
 * 
 *                          RETRIEVED VIA:
 *                          - @AuthenticationPrincipal in controllers (Spring
 *                          Security standard)
 * 
 *                          WHY CUSTOM AUTHENTICATION:
 *                          - Type safety: getUserId() returns Long, not Object
 *                          casting
 *                          - Domain-specific: hasRole(), isAdmin() helper
 *                          methods
 *                          - Testable: Easy to mock in unit tests
 *                          - Standard: Uses Spring Security patterns
 *                          (@AuthenticationPrincipal)
 */
public class CustomUserAuthentication implements Authentication {

    private final Long userId;
    private final String email;
    private final String role;
    private boolean authenticated = true;

    /**
     * Constructor for regular user authentication (from Gateway)
     * 
     * @param userId User ID from JWT (via Gateway headers)
     * @param email  User email from JWT
     * @param role   User role (USER, ADMIN, etc)
     */
    public CustomUserAuthentication(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    /**
     * Constructor for service-to-service authentication
     * Used when other microservices call PaymentService with API key.
     * 
     * @param serviceName Service identifier (e.g., "INTERNAL_SERVICE")
     * @param role        Always "INTERNAL_SERVICE"
     */
    public CustomUserAuthentication(String serviceName, String role) {
        this.userId = null; // Services don't have user ID
        this.email = serviceName;
        this.role = role;
    }

    // ==========================================
    // CUSTOM HELPER METHODS
    // ==========================================

    /**
     * Get authenticated user's ID
     * 
     * @return User ID (null for service-to-service calls)
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Get authenticated user's email
     * 
     * @return Email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get authenticated user's role
     * 
     * @return Role string (USER, ADMIN, INTERNAL_SERVICE)
     */
    public String getRole() {
        return role;
    }

    /**
     * Check if user has specific role
     * 
     * @param roleToCheck Role to check (without ROLE_ prefix)
     * @return true if user has role
     */
    public boolean hasRole(String roleToCheck) {
        return role != null && role.equalsIgnoreCase(roleToCheck);
    }

    /**
     * Check if user is admin
     * 
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if this is service-to-service authentication
     * 
     * @return true if INTERNAL_SERVICE role
     */
    public boolean isInternalService() {
        return hasRole("INTERNAL_SERVICE");
    }

    /**
     * Check if user is admin OR internal service (full access)
     * 
     * @return true if admin or service
     */
    public boolean hasAdminAccess() {
        return isAdmin() || isInternalService();
    }

    // ==========================================
    // SPRING SECURITY AUTHENTICATION INTERFACE
    // ==========================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public Object getCredentials() {
        return null; // No password in JWT architecture
    }

    @Override
    public Object getDetails() {
        return null; // No additional details needed
    }

    /**
     * CRITICAL: Return 'this' for @AuthenticationPrincipal to work
     * 
     * When Spring Security sees @AuthenticationPrincipal, it calls
     * Authentication.getPrincipal(). By returning 'this', the controller
     * receives the CustomUserAuthentication object directly.
     */
    @Override
    public Object getPrincipal() {
        return this; // Return the CustomUserAuthentication itself for @AuthenticationPrincipal
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String toString() {
        return "CustomUserAuthentication{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", authenticated=" + authenticated +
                '}';
    }
}
