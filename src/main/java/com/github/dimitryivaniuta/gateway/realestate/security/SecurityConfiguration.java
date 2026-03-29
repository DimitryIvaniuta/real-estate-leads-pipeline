package com.github.dimitryivaniuta.gateway.realestate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

/**
 * Reactive security configuration with dedicated chains for browser portal and APIs.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Portal chain uses session authentication and CSRF protection.
     *
     * @param http security builder
     * @return configured chain
     */
    @Bean
    @Order(1)
    public SecurityWebFilterChain portalSecurityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/portal/**"))
                .authorizeExchange(spec -> spec
                        .pathMatchers("/portal/csrf", "/login").permitAll()
                        .anyExchange().authenticated())
                .formLogin(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; base-uri 'self'"))
                        .referrerPolicy(policy -> policy.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.SAME_ORIGIN))
                        .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::deny))
                .build();
    }

    /**
     * API chain uses stateless JWT bearer tokens and role-based access control.
     *
     * @param http security builder
     * @param jwtRoleConverter custom role converter
     * @return configured chain
     */
    @Bean
    @Order(2)
    public SecurityWebFilterChain apiSecurityWebFilterChain(final ServerHttpSecurity http,
                                                            final JwtRoleConverter jwtRoleConverter) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**"))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(spec -> spec
                        .pathMatchers(HttpMethod.GET, "/api/**").hasAnyRole("AGENT", "MANAGER")
                        .pathMatchers("/api/**").hasAnyRole("AGENT", "MANAGER")
                        .anyExchange().authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new ReactiveJwtAuthenticationConverterAdapter(jwtRoleConverter))))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(policy -> policy.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.NO_REFERRER))
                        .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::deny))
                .build();
    }

    /**
     * Fallback chain for actuator health and any unmatched routes.
     *
     * @param http security builder
     * @return configured chain
     */
    @Bean
    @Order(3)
    public SecurityWebFilterChain fallbackSecurityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .authorizeExchange(spec -> spec
                        .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyExchange().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Demo portal users for local development.
     *
     * @return reactive user details service
     */
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        final UserDetails agent = User.withUsername("agent")
                .password("{noop}agent-password")
                .roles("AGENT")
                .build();
        final UserDetails manager = User.withUsername("manager")
                .password("{noop}manager-password")
                .roles("MANAGER")
                .build();
        return new MapReactiveUserDetailsService(agent, manager);
    }
}
