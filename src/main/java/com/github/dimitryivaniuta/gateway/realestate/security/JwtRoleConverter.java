package com.github.dimitryivaniuta.gateway.realestate.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Converts JWT role and scope claims into Spring Security authorities.
 */
@Component
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final List<String> roles = jwt.getClaimAsStringList("roles");
        final String scopeValue = jwt.getClaimAsString("scope");
        final List<String> scopes = scopeValue == null || scopeValue.isBlank()
                ? List.of()
                : List.of(scopeValue.split(" "));

        final Collection<GrantedAuthority> authorities = Stream.concat(
                        roles == null ? Stream.empty() : roles.stream().map(role -> "ROLE_" + role),
                        scopes.stream().map(scope -> "SCOPE_" + scope))
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
