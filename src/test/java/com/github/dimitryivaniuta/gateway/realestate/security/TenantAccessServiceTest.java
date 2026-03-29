package com.github.dimitryivaniuta.gateway.realestate.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Tests for tenant access validation.
 */
class TenantAccessServiceTest {

    private final TenantAccessService service = new TenantAccessService();

    @Test
    void shouldAllowManagerForAnyTenant() {
        final JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt(UUID.randomUUID()),
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")), "manager");

        assertThatCode(() -> service.assertTenantAccess(authentication, UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectAgentForDifferentTenant() {
        final UUID tokenTenantId = UUID.randomUUID();
        final JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt(tokenTenantId),
                List.of(new SimpleGrantedAuthority("ROLE_AGENT")), "agent");

        assertThatThrownBy(() -> service.assertTenantAccess(authentication, UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    private Jwt jwt(final UUID tenantId) {
        return new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(3600),
                java.util.Map.of("alg", "none"),
                java.util.Map.of("sub", "agent-1", "tenant_id", tenantId.toString()));
    }
}
