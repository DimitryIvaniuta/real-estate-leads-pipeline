package com.github.dimitryivaniuta.gateway.realestate.security;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Validates that the caller can access the requested tenant.
 */
@Component
public class TenantAccessService {

    /**
     * Ensures the tenant requested in the URI matches the tenant claim for agent users.
     * Managers may access any tenant when they have the manager role.
     *
     * @param authentication authenticated JWT token
     * @param requestedTenantId tenant from request
     */
    public void assertTenantAccess(final JwtAuthenticationToken authentication, final UUID requestedTenantId) {
        if (authentication == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        final boolean manager = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority()));
        if (manager) {
            return;
        }
        final String tenantClaim = authentication.getToken().getClaimAsString("tenant_id");
        if (tenantClaim == null || !requestedTenantId.toString().equals(tenantClaim)) {
            throw new AccessDeniedException("Tenant access denied");
        }
    }
}
