package com.github.dimitryivaniuta.gateway.realestate.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for suspicious request filtering.
 */
@ExtendWith(MockitoExtension.class)
class SuspiciousRequestFilterTest {

    @Mock
    private IpAddressResolver ipAddressResolver;

    @Mock
    private IpThreatProtectionService ipThreatProtectionService;

    @Mock
    private RedisIpRateLimiter redisIpRateLimiter;

    private final AppProperties properties = new AppProperties(
            new AppProperties.Security(List.of("/xmlrpc.php"), Duration.ofMinutes(30), 3),
            new AppProperties.RateLimit(60, 120),
            new AppProperties.Portal(List.of("http://localhost:8080")),
            new AppProperties.Topics("lead.phase.changed", "security.threat.detected"));

    @Test
    void shouldRejectKnownSuspiciousPath() {
        final SuspiciousRequestFilter filter = new SuspiciousRequestFilter(
                properties,
                ipAddressResolver,
                ipThreatProtectionService,
                redisIpRateLimiter);
        final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/xmlrpc.php"));
        final WebFilterChain chain = ignored -> Mono.error(new IllegalStateException("should not continue"));

        when(ipAddressResolver.resolve(exchange.getRequest())).thenReturn("1.2.3.4");
        when(ipThreatProtectionService.isBlocked("1.2.3.4")).thenReturn(Mono.just(false));
        when(ipThreatProtectionService.recordSuspiciousActivity(anyString(), anyString(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRejectAlreadyBlockedIp() {
        final SuspiciousRequestFilter filter = new SuspiciousRequestFilter(
                properties,
                ipAddressResolver,
                ipThreatProtectionService,
                redisIpRateLimiter);
        final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/leads"));
        final WebFilterChain chain = ignored -> Mono.error(new IllegalStateException("should not continue"));

        when(ipAddressResolver.resolve(exchange.getRequest())).thenReturn("1.2.3.4");
        when(ipThreatProtectionService.isBlocked("1.2.3.4")).thenReturn(Mono.just(true));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
